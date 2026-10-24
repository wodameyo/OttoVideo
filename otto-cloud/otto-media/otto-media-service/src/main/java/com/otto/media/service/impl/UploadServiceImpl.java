package com.otto.media.service.impl;

import com.otto.common.constants.Constants;
import com.otto.common.entity.enums.DateTimePatternEnum;
import com.otto.common.entity.enums.ResponseCodeEnum;
import com.otto.common.entity.vo.ResponseVO;
import com.otto.common.exception.BusinessException;
import com.otto.common.redis.RedisUtils;
import com.otto.common.utils.DateUtil;
import com.otto.common.utils.StringTools;
import com.otto.media.config.MinioProperties;
import com.otto.media.config.UploadProperties;
import com.otto.media.entity.dto.UploadHashCacheDTO;
import com.otto.media.entity.dto.UploadInitDTO;
import com.otto.media.entity.dto.UploadSessionDTO;
import com.otto.media.entity.enums.UploadStatusEnum;
import com.otto.media.entity.vo.UploadCompleteVO;
import com.otto.media.entity.vo.UploadInitVO;
import com.otto.media.entity.vo.UploadProgressVO;
import com.otto.media.mq.VideoTranscodeMessage;
import com.otto.media.service.HashCacheService;
import com.otto.media.service.StorageService;
import com.otto.media.service.UploadService;
import com.otto.media.utils.FileTools;
import com.otto.video.api.VideoFeignClient;
import com.otto.video.api.dto.VideoBaseInfoDTO;
import com.otto.video.api.dto.VideoFileDTO;
import com.otto.video.api.mq.VideoTranscodeResultMessage;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

/**
 * 分片上传实现：本地临时目录存分片，完成后合并上传 MinIO 并投递转码消息
 */
@Service
public class UploadServiceImpl implements UploadService {

    private static final Logger logger = LoggerFactory.getLogger(UploadServiceImpl.class);

    /**
     * uploadId / hash 格式校验，防止路径穿越
     */
    private static final String UPLOAD_ID_PATTERN = "[a-fA-F0-9]{32}";

    private static final String HASH_PATTERN = "[a-fA-F0-9]{32}";

    private static final int MAX_CHUNK_TOTAL = 4096;

    /**
     * 稿件允许上传的状态：0 转码中 / 1 转码失败
     */
    private static final int VIDEO_STATUS_TRANSCODING = 0;

    private static final int VIDEO_STATUS_TRANSCODE_FAILED = 1;

    /**
     * 分P状态：0 转码中
     */
    private static final int FILE_STATUS_TRANSCODING = 0;

    /**
     * 分P状态：2 转码失败（允许重新上传）
     */
    private static final int FILE_STATUS_TRANSCODE_FAILED = 2;

    @Resource
    private UploadProperties uploadProperties;

    @Resource
    private MinioProperties minioProperties;

    @Resource
    private StorageService storageService;

    @Resource
    private HashCacheService hashCacheService;

    @Resource
    private VideoFeignClient videoFeignClient;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private RedisUtils redisUtils;

    @Override
    public UploadInitVO init(UploadInitDTO dto, String userId) {
        if (dto == null || StringTools.isEmpty(dto.getVideoId()) || StringTools.isEmpty(dto.getFileId())) {
            throw new BusinessException("videoId 与 fileId 不能为空");
        }
        if (StringTools.isEmpty(dto.getFileName()) || dto.getFileName().length() > 255) {
            throw new BusinessException("文件名不合法");
        }
        String ext = extension(dto.getFileName());
        if (!uploadProperties.getAllowedExtensions().contains(ext)) {
            throw new BusinessException("仅支持 " + String.join("/", uploadProperties.getAllowedExtensions()) + " 格式");
        }
        if (dto.getFileSize() == null || dto.getFileSize() <= 0
                || dto.getFileSize() > uploadProperties.getMaxFileSize()) {
            throw new BusinessException("文件大小超出限制（最大 " + uploadProperties.getMaxFileSize() / 1024 / 1024 + "MB）");
        }
        if (dto.getChunkTotal() == null || dto.getChunkTotal() < 1 || dto.getChunkTotal() > MAX_CHUNK_TOTAL) {
            throw new BusinessException("分片数量不合法");
        }
        String hash = dto.getHash();
        if (StringTools.isNotEmpty(hash) && !hash.matches(HASH_PATTERN)) {
            throw new BusinessException("文件 hash 格式不正确");
        }

        // 校验分P归属：fileId 属于 videoId，videoId 属于当前登录用户
        VideoFileDTO videoFile = requireData(videoFeignClient.getFile(dto.getFileId()), "分P不存在");
        if (!dto.getVideoId().equals(videoFile.getVideoId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_403);
        }
        if (!Integer.valueOf(FILE_STATUS_TRANSCODING).equals(videoFile.getStatus())
                && !Integer.valueOf(FILE_STATUS_TRANSCODE_FAILED).equals(videoFile.getStatus())) {
            throw new BusinessException("该分P已上传，请勿重复上传");
        }
        VideoBaseInfoDTO video = requireData(videoFeignClient.getById(dto.getVideoId()), "稿件不存在");
        if (!userId.equals(video.getUserId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_403);
        }
        if (!Integer.valueOf(VIDEO_STATUS_TRANSCODING).equals(video.getStatus())
                && !Integer.valueOf(VIDEO_STATUS_TRANSCODE_FAILED).equals(video.getStatus())) {
            throw new BusinessException("当前稿件状态不允许上传");
        }

        UploadInitVO vo = new UploadInitVO();
        vo.setVideoId(dto.getVideoId());
        vo.setFileId(dto.getFileId());
        vo.setChunkTotal(dto.getChunkTotal());
        vo.setChunkSize(uploadProperties.getChunkSize().intValue());

        // 秒传：命中已完成转码的 hash，直接投递成功结果
        if (StringTools.isNotEmpty(hash)) {
            UploadHashCacheDTO cache = hashCacheService.get(hash);
            if (cache != null && StringTools.isNotEmpty(cache.getHlsPath())) {
                publishInstantResult(dto, cache);
                vo.setInstant(true);
                vo.setHlsPath(cache.getHlsPath());
                vo.setCoverPath(cache.getCoverPath());
                vo.setDuration(cache.getDuration());
                vo.setResolution(cache.getResolution());
                vo.setFileSize(cache.getFileSize() == null ? dto.getFileSize() : cache.getFileSize());
                logger.info("秒传命中：videoId={}，fileId={}，hash={}", dto.getVideoId(), dto.getFileId(), hash);
                return vo;
            }
        }

        String uploadId = StringTools.getUUID();
        UploadSessionDTO session = new UploadSessionDTO();
        session.setUploadId(uploadId);
        session.setVideoId(dto.getVideoId());
        session.setFileId(dto.getFileId());
        session.setUserId(userId);
        session.setFileName(dto.getFileName());
        session.setFileSize(dto.getFileSize());
        session.setChunkTotal(dto.getChunkTotal());
        session.setHash(StringTools.isEmpty(hash) ? null : hash.toLowerCase());
        session.setStatus(UploadStatusEnum.UPLOADING.getCode());
        session.setCreatedAt(System.currentTimeMillis());
        saveSession(session);
        createTempDir(uploadId);

        vo.setUploadId(uploadId);
        vo.setInstant(false);
        vo.setUploadedChunks(new ArrayList<>());
        return vo;
    }

    @Override
    public UploadProgressVO uploadChunk(String uploadId, Integer index, MultipartFile file, String userId) {
        UploadSessionDTO session = getSession(uploadId);
        checkOwner(session, userId);
        if (!UploadStatusEnum.UPLOADING.getCode().equals(session.getStatus())) {
            throw new BusinessException("当前上传会话不可写入分片");
        }
        if (index == null || index < 0 || index >= session.getChunkTotal()) {
            throw new BusinessException("分片序号不合法");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException("分片内容为空");
        }
        if (file.getSize() > uploadProperties.getChunkSize()) {
            throw new BusinessException("分片大小超出限制");
        }
        Path dir = createTempDir(uploadId);
        Path target = dir.resolve(index + ".part");
        try {
            file.transferTo(target);
        } catch (Exception e) {
            throw new BusinessException("分片写入失败", e);
        }
        return buildProgress(session, uploadedIndexes(uploadId));
    }

    @Override
    public UploadCompleteVO complete(String uploadId, String userId) {
        UploadSessionDTO session = getSession(uploadId);
        checkOwner(session, userId);
        if (UploadStatusEnum.COMPLETED.getCode().equals(session.getStatus())) {
            throw new BusinessException("该上传已处理完成，请勿重复提交");
        }
        if (!UploadStatusEnum.UPLOADING.getCode().equals(session.getStatus())) {
            throw new BusinessException("当前上传会话不可合并");
        }

        List<Integer> indexes = uploadedIndexes(uploadId);
        if (indexes.size() != session.getChunkTotal()) {
            throw new BusinessException("分片不完整，已上传 " + indexes.size() + "/" + session.getChunkTotal());
        }

        session.setStatus(UploadStatusEnum.MERGING.getCode());
        saveSession(session);

        Path dir = tempDir(uploadId);
        String ext = extension(session.getFileName());
        Path merged = dir.resolve("origin." + ext);
        try {
            mergeChunks(dir, session.getChunkTotal(), merged);
            long actualSize = Files.size(merged);
            if (actualSize > uploadProperties.getMaxFileSize()) {
                throw new BusinessException("文件大小超出限制");
            }

            String objectName = buildOriginObjectName(session.getFileId(), ext);
            storageService.uploadFile(minioProperties.getVideoBucket(), objectName, merged, contentType(ext));

            // 重新上传时清除旧的转码幂等标记，允许失败后重试
            redisUtils.delete(Constants.REDIS_KEY_TRANSCODE_TASK + session.getFileId());
            publishTranscode(session, objectName, actualSize);

            session.setStatus(UploadStatusEnum.COMPLETED.getCode());
            saveSession(session);
            deleteDir(dir);

            UploadCompleteVO vo = new UploadCompleteVO();
            vo.setVideoId(session.getVideoId());
            vo.setFileId(session.getFileId());
            vo.setOriginPath(objectName);
            vo.setFileSize(actualSize);
            logger.info("上传完成：videoId={}，fileId={}，origin={}", session.getVideoId(), session.getFileId(), objectName);
            return vo;
        } catch (BusinessException e) {
            session.setStatus(UploadStatusEnum.UPLOADING.getCode());
            saveSession(session);
            throw e;
        } catch (Exception e) {
            session.setStatus(UploadStatusEnum.UPLOADING.getCode());
            saveSession(session);
            throw new BusinessException("合并上传失败", e);
        }
    }

    @Override
    public UploadProgressVO status(String uploadId, String userId) {
        UploadSessionDTO session = getSession(uploadId);
        checkOwner(session, userId);
        return buildProgress(session, uploadedIndexes(uploadId));
    }

    @Override
    public void cancel(String uploadId, String userId) {
        UploadSessionDTO session = getSession(uploadId);
        checkOwner(session, userId);
        if (UploadStatusEnum.COMPLETED.getCode().equals(session.getStatus())) {
            throw new BusinessException("上传已完成，无法取消");
        }
        deleteDir(tempDir(uploadId));
        session.setStatus(UploadStatusEnum.CANCELED.getCode());
        saveSession(session);
        logger.info("上传取消：uploadId={}，fileId={}", uploadId, session.getFileId());
    }

    // ============ 内部方法 ============

    private UploadProgressVO buildProgress(UploadSessionDTO session, List<Integer> uploaded) {
        UploadProgressVO vo = new UploadProgressVO();
        vo.setUploadId(session.getUploadId());
        vo.setStatus(session.getStatus());
        vo.setChunkTotal(session.getChunkTotal());
        vo.setUploadedChunks(uploaded);
        return vo;
    }

    private void mergeChunks(Path dir, int chunkTotal, Path merged) throws IOException {
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(merged))) {
            byte[] buffer = new byte[8192];
            for (int i = 0; i < chunkTotal; i++) {
                Path part = dir.resolve(i + ".part");
                if (!Files.isRegularFile(part)) {
                    throw new BusinessException("分片缺失：" + i);
                }
                try (InputStream in = Files.newInputStream(part)) {
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                }
            }
        }
    }

    /**
     * 以磁盘上的分片为准，避免并发上报导致的进度丢失
     */
    private List<Integer> uploadedIndexes(String uploadId) {
        Path dir = tempDir(uploadId);
        List<Integer> result = new ArrayList<>();
        if (!Files.isDirectory(dir)) {
            return result;
        }
        try (Stream<Path> stream = Files.list(dir)) {
            stream.filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.endsWith(".part"))
                    .map(name -> name.substring(0, name.length() - ".part".length()))
                    .filter(name -> name.matches("\\d+"))
                    .map(Integer::valueOf)
                    .distinct()
                    .sorted()
                    .forEach(result::add);
        } catch (IOException e) {
            throw new BusinessException("读取分片进度失败", e);
        }
        return result;
    }

    private void publishTranscode(UploadSessionDTO session, String objectName, long fileSize) {
        VideoTranscodeMessage message = new VideoTranscodeMessage();
        message.setVideoId(session.getVideoId());
        message.setFileId(session.getFileId());
        message.setBucket(minioProperties.getVideoBucket());
        message.setObjectName(objectName);
        message.setFileName(session.getFileName());
        message.setFileSize(fileSize);
        message.setHash(session.getHash());
        rabbitTemplate.convertAndSend(Constants.MQ_EXCHANGE_VIDEO, Constants.MQ_ROUTING_TRANSCODE, message);
    }

    private void publishInstantResult(UploadInitDTO dto, UploadHashCacheDTO cache) {
        VideoTranscodeResultMessage message = new VideoTranscodeResultMessage();
        message.setVideoId(dto.getVideoId());
        message.setFileId(dto.getFileId());
        message.setStatus(1);
        message.setDuration(cache.getDuration());
        message.setHlsPath(cache.getHlsPath());
        message.setCoverPath(cache.getCoverPath());
        message.setResolution(cache.getResolution());
        message.setFileSize(cache.getFileSize() == null ? dto.getFileSize() : cache.getFileSize());
        rabbitTemplate.convertAndSend(Constants.MQ_EXCHANGE_VIDEO, Constants.MQ_ROUTING_TRANSCODE_RESULT, message);
    }

    private UploadSessionDTO getSession(String uploadId) {
        if (StringTools.isEmpty(uploadId) || !uploadId.matches(UPLOAD_ID_PATTERN)) {
            throw new BusinessException("uploadId 不合法");
        }
        UploadSessionDTO session = redisUtils.get(Constants.REDIS_KEY_UPLOAD_SESSION + uploadId,
                UploadSessionDTO.class);
        if (session == null) {
            throw new BusinessException("上传会话不存在或已过期");
        }
        return session;
    }

    private void saveSession(UploadSessionDTO session) {
        redisUtils.setex(Constants.REDIS_KEY_UPLOAD_SESSION + session.getUploadId(), session,
                Constants.REDIS_KEY_EXPIRES_ONE_DAY);
    }

    private void checkOwner(UploadSessionDTO session, String userId) {
        if (StringTools.isEmpty(userId) || !userId.equals(session.getUserId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_403);
        }
    }

    private Path createTempDir(String uploadId) {
        Path dir = tempDir(uploadId);
        try {
            Files.createDirectories(dir);
            return dir;
        } catch (IOException e) {
            throw new BusinessException("创建临时目录失败", e);
        }
    }

    private Path tempDir(String uploadId) {
        return Paths.get(uploadProperties.getTempDir(), uploadId).toAbsolutePath().normalize();
    }

    private void deleteDir(Path dir) {
        if (dir != null && !FileTools.deleteDirectory(dir)) {
            logger.warn("临时目录清理失败：{}", dir);
        }
    }

    private String buildOriginObjectName(String fileId, String ext) {
        String month = DateUtil.format(new Date(), DateTimePatternEnum.YYYYMM.getPattern());
        return "video/" + month + "/" + fileId + "/origin." + ext;
    }

    private String extension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            throw new BusinessException("文件缺少后缀");
        }
        return fileName.substring(index + 1).toLowerCase();
    }

    private String contentType(String ext) {
        return switch (ext) {
            case "mp4" -> "video/mp4";
            case "flv" -> "video/x-flv";
            case "mkv" -> "video/x-matroska";
            case "mov" -> "video/quicktime";
            default -> "application/octet-stream";
        };
    }

    private <T> T requireData(ResponseVO<T> response, String emptyMsg) {
        if (response == null) {
            throw new BusinessException(emptyMsg);
        }
        if (!ResponseCodeEnum.CODE_200.getCode().equals(response.getCode())) {
            throw new BusinessException(response.getCode(),
                    StringTools.isEmpty(response.getInfo()) ? emptyMsg : response.getInfo());
        }
        if (response.getData() == null) {
            throw new BusinessException(emptyMsg);
        }
        return response.getData();
    }
}
