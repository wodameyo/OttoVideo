package com.otto.media.service.impl;

import com.otto.common.constants.Constants;
import com.otto.common.entity.enums.DateTimePatternEnum;
import com.otto.common.exception.BusinessException;
import com.otto.common.utils.DateUtil;
import com.otto.common.utils.StringTools;
import com.otto.media.config.MinioProperties;
import com.otto.media.config.TranscodeProperties;
import com.otto.media.entity.dto.MediaInfoDTO;
import com.otto.media.entity.dto.RenditionDTO;
import com.otto.media.entity.dto.UploadHashCacheDTO;
import com.otto.media.mq.VideoTranscodeMessage;
import com.otto.media.service.FfmpegService;
import com.otto.media.service.HashCacheService;
import com.otto.media.service.StorageService;
import com.otto.media.service.TranscodeService;
import com.otto.media.utils.FileTools;
import com.otto.video.api.mq.VideoTranscodeResultMessage;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * 转码编排实现
 */
@Service
public class TranscodeServiceImpl implements TranscodeService {

    private static final Logger logger = LoggerFactory.getLogger(TranscodeServiceImpl.class);

    private static final String TASK_PROCESSING = "PROCESSING";

    private static final String TASK_SUCCESS = "SUCCESS";

    private static final String TASK_FAILED = "FAILED";

    /**
     * fileId 作为目录名，限制字符集防止路径穿越
     */
    private static final String FILE_ID_PATTERN = "[A-Za-z0-9_-]{1,64}";

    @Resource
    private TranscodeProperties transcodeProperties;

    @Resource
    private MinioProperties minioProperties;

    @Resource
    private FfmpegService ffmpegService;

    @Resource
    private StorageService storageService;

    @Resource
    private HashCacheService hashCacheService;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void transcode(VideoTranscodeMessage message) {
        if (message == null || StringTools.isEmpty(message.getFileId())
                || !message.getFileId().matches(FILE_ID_PATTERN)
                || StringTools.isEmpty(message.getObjectName())) {
            logger.warn("转码消息不合法，已丢弃");
            return;
        }
        String fileId = message.getFileId();
        String taskKey = Constants.REDIS_KEY_TRANSCODE_TASK + fileId;
        if (!markTask(taskKey, TASK_PROCESSING)) {
            logger.info("转码任务已存在，跳过重复消息：fileId={}", fileId);
            return;
        }

        long startTime = System.currentTimeMillis();
        Path workDir = Paths.get(transcodeProperties.getWorkDir(), fileId).toAbsolutePath().normalize();
        try {
            FileTools.deleteDirectory(workDir);
            Files.createDirectories(workDir);

            // 1. 下载原片
            String ext = extension(message.getFileName());
            Path origin = workDir.resolve("origin." + ext);
            storageService.downloadToFile(message.getBucket(), message.getObjectName(), origin);
            long originSize = Files.size(origin);

            // 2. 探测
            MediaInfoDTO mediaInfo = ffmpegService.probe(origin);

            // 3. 逐清晰度转 HLS
            List<RenditionDTO> renditions = buildRenditions(mediaInfo);
            Path hlsDir = workDir.resolve("hls");
            Files.createDirectories(hlsDir);
            for (RenditionDTO rendition : renditions) {
                Path outputDir = hlsDir.resolve(rendition.getName());
                Files.createDirectories(outputDir);
                ffmpegService.transcodeHls(origin, outputDir, rendition.getHeight(),
                        rendition.getVideoBitrate(), rendition.getAudioBitrate(),
                        workDir.resolve("ffmpeg-" + rendition.getName() + ".log"));
            }
            writeMasterPlaylist(hlsDir, renditions);

            // 4. 封面截图
            Path cover = workDir.resolve("cover.jpg");
            int seekSecond = mediaInfo.getDuration() != null && mediaInfo.getDuration() > 2 ? 1 : 0;
            ffmpegService.snapshot(origin, cover, seekSecond);

            // 5. 上传产物
            String month = DateUtil.format(new Date(), DateTimePatternEnum.YYYYMM.getPattern());
            String hlsPrefix = "video/" + month + "/" + fileId + "/hls";
            uploadHls(hlsDir, hlsPrefix);
            String coverPath = "cover/" + month + "/" + fileId + ".jpg";
            storageService.uploadFile(minioProperties.getCoverBucket(), coverPath, cover, "image/jpeg");

            String hlsPath = hlsPrefix + "/index.m3u8";

            // 6. 原片处理
            if (!Boolean.TRUE.equals(transcodeProperties.getKeepOrigin())) {
                try {
                    storageService.removeObject(message.getBucket(), message.getObjectName());
                } catch (Exception e) {
                    logger.warn("原片删除失败，忽略：{}", message.getObjectName());
                }
            }

            // 7. 秒传缓存
            if (StringTools.isNotEmpty(message.getHash())) {
                UploadHashCacheDTO cache = new UploadHashCacheDTO();
                cache.setHash(message.getHash());
                cache.setHlsPath(hlsPath);
                cache.setCoverPath(coverPath);
                cache.setDuration(mediaInfo.getDuration());
                cache.setResolution(mediaInfo.getResolution());
                cache.setFileSize(originSize);
                hashCacheService.put(message.getHash(), cache);
            }

            markTask(taskKey, TASK_SUCCESS);
            publishResult(message, 1, mediaInfo.getDuration(), hlsPath, coverPath,
                    mediaInfo.getResolution(), originSize, null);
            logger.info("转码完成：fileId={}，清晰度 {}，耗时 {} ms",
                    fileId, renditions.size(), System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            logger.error("转码失败：fileId={}", fileId, e);
            markTask(taskKey, TASK_FAILED);
            publishResult(message, 2, null, null, null, null,
                    message.getFileSize(), truncate(e.getMessage()));
        } finally {
            FileTools.deleteDirectory(workDir);
        }
    }

    // ============ 内部方法 ============

    /**
     * 以 SETNX 抢占任务，已存在则视为重复消息
     */
    private boolean markTask(String taskKey, String value) {
        long expireSeconds = transcodeProperties.getTaskExpireDays() * 24L * 3600L;
        if (TASK_PROCESSING.equals(value)) {
            return Boolean.TRUE.equals(stringRedisTemplate.opsForValue()
                    .setIfAbsent(taskKey, value, expireSeconds, TimeUnit.SECONDS));
        }
        stringRedisTemplate.opsForValue().set(taskKey, value, expireSeconds, TimeUnit.SECONDS);
        return true;
    }

    private void publishResult(VideoTranscodeMessage message, int status, Integer duration,
                               String hlsPath, String coverPath, String resolution,
                               Long fileSize, String errorMsg) {
        VideoTranscodeResultMessage result = new VideoTranscodeResultMessage();
        result.setVideoId(message.getVideoId());
        result.setFileId(message.getFileId());
        result.setStatus(status);
        result.setDuration(duration);
        result.setHlsPath(hlsPath);
        result.setCoverPath(coverPath);
        result.setResolution(resolution);
        result.setFileSize(fileSize);
        result.setErrorMsg(errorMsg);
        try {
            rabbitTemplate.convertAndSend(Constants.MQ_EXCHANGE_VIDEO,
                    Constants.MQ_ROUTING_TRANSCODE_RESULT, result);
        } catch (Exception e) {
            logger.error("转码结果投递失败：fileId={}，status={}", message.getFileId(), status, e);
        }
    }

    private List<RenditionDTO> buildRenditions(MediaInfoDTO mediaInfo) {
        int height = mediaInfo.getHeight();
        List<RenditionDTO> renditions = new ArrayList<>();
        if (height >= 720) {
            renditions.add(buildRendition("720p", mediaInfo, 720, "2200k", "128k", 2400000));
        }
        if (height >= 480) {
            renditions.add(buildRendition("480p", mediaInfo, 480, "1000k", "96k", 1100000));
        }
        if (renditions.isEmpty()) {
            renditions.add(buildRendition("source", mediaInfo, null, "1200k", "96k", 1300000));
        }
        return renditions;
    }

    private RenditionDTO buildRendition(String name, MediaInfoDTO mediaInfo, Integer targetHeight,
                                        String videoBitrate, String audioBitrate, Integer bandwidth) {
        RenditionDTO rendition = new RenditionDTO();
        rendition.setName(name);
        rendition.setVideoBitrate(videoBitrate);
        rendition.setAudioBitrate(audioBitrate);
        rendition.setBandwidth(bandwidth);
        if (targetHeight == null) {
            rendition.setWidth(mediaInfo.getWidth());
            rendition.setHeight(mediaInfo.getHeight());
        } else {
            rendition.setHeight(targetHeight);
            int scaledWidth = (int) Math.round(mediaInfo.getWidth() * targetHeight / (double) mediaInfo.getHeight());
            rendition.setWidth(Math.max(2, scaledWidth % 2 == 0 ? scaledWidth : scaledWidth - 1));
        }
        return rendition;
    }

    private void writeMasterPlaylist(Path hlsDir, List<RenditionDTO> renditions) throws IOException {
        StringBuilder master = new StringBuilder("#EXTM3U\n#EXT-X-VERSION:3\n");
        for (RenditionDTO rendition : renditions) {
            master.append("#EXT-X-STREAM-INF:BANDWIDTH=").append(rendition.getBandwidth())
                    .append(",RESOLUTION=").append(rendition.getWidth()).append("x").append(rendition.getHeight())
                    .append("\n").append(rendition.getName()).append("/index.m3u8\n");
        }
        Files.writeString(hlsDir.resolve("index.m3u8"), master.toString());
    }

    private void uploadHls(Path hlsDir, String hlsPrefix) throws IOException {
        try (Stream<Path> stream = Files.walk(hlsDir)) {
            List<Path> files = stream.filter(Files::isRegularFile).sorted().toList();
            for (Path file : files) {
                String relative = hlsDir.relativize(file).toString().replace('\\', '/');
                storageService.uploadFile(minioProperties.getVideoBucket(), hlsPrefix + "/" + relative,
                        file, hlsContentType(relative));
            }
        }
    }

    private String hlsContentType(String relative) {
        if (relative.endsWith(".m3u8")) {
            return "application/vnd.apple.mpegurl";
        }
        if (relative.endsWith(".ts")) {
            return "video/mp2t";
        }
        return "application/octet-stream";
    }

    private String extension(String fileName) {
        if (StringTools.isEmpty(fileName)) {
            return "mp4";
        }
        int index = fileName.lastIndexOf('.');
        return index < 0 ? "mp4" : fileName.substring(index + 1).toLowerCase();
    }

    private String truncate(String message) {
        if (message == null || message.isBlank()) {
            return "转码失败";
        }
        return message.length() <= 400 ? message : message.substring(0, 400);
    }
}
