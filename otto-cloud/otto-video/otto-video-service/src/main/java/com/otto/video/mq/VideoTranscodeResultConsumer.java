package com.otto.video.mq;

import com.otto.common.constants.Constants;
import com.otto.common.redis.RedisUtils;
import com.otto.video.api.mq.VideoTranscodeResultMessage;
import com.otto.video.entity.enums.VideoFileStatusEnum;
import com.otto.video.entity.enums.VideoStatusEnum;
import com.otto.video.entity.po.VideoFile;
import com.otto.video.entity.po.VideoInfo;
import com.otto.video.entity.query.VideoFileQuery;
import com.otto.video.entity.query.VideoInfoQuery;
import com.otto.video.mappers.VideoFileMapper;
import com.otto.video.mappers.VideoInfoMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 转码结果消费者：更新分P信息，并推进稿件整体状态
 */
@Component
public class VideoTranscodeResultConsumer {

    private static final Logger logger = LoggerFactory.getLogger(VideoTranscodeResultConsumer.class);

    @Resource
    private VideoFileMapper videoFileMapper;

    @Resource
    private VideoInfoMapper videoInfoMapper;

    @Resource
    private RedisUtils redisUtils;

    @RabbitListener(queues = Constants.MQ_QUEUE_TRANSCODE_RESULT)
    @Transactional(rollbackFor = Exception.class)
    public void onTranscodeResult(VideoTranscodeResultMessage message) {
        if (message == null || message.getFileId() == null || message.getVideoId() == null) {
            logger.warn("忽略无效转码结果消息: {}", message);
            return;
        }
        VideoFile file = selectFile(message.getFileId());
        if (file == null) {
            logger.warn("转码结果对应的分P不存在, fileId={}", message.getFileId());
            return;
        }
        if (!VideoFileStatusEnum.TRANSCODING.getStatus().equals(file.getStatus())) {
            // 幂等：已处理过的结果直接忽略
            logger.info("分P转码结果重复, fileId={}, status={}", file.getFileId(), file.getStatus());
            return;
        }

        VideoFile update = new VideoFile();
        update.setStatus(message.getStatus());
        if (VideoFileStatusEnum.SUCCESS.getStatus().equals(message.getStatus())) {
            update.setDuration(message.getDuration() == null ? 0 : message.getDuration());
            update.setHlsPath(message.getHlsPath());
            update.setCoverPath(message.getCoverPath());
            update.setResolution(message.getResolution());
            if (message.getFileSize() != null && message.getFileSize() > 0) {
                update.setFileSize(message.getFileSize());
            }
        } else {
            logger.error("分P转码失败, fileId={}, videoId={}, reason={}",
                    message.getFileId(), message.getVideoId(), message.getErrorMsg());
        }
        VideoFileQuery fileQuery = new VideoFileQuery();
        fileQuery.setFileId(message.getFileId());
        videoFileMapper.updateByParam(update, fileQuery);

        refreshVideoStatus(message.getVideoId());
        redisUtils.delete(Constants.REDIS_KEY_VIDEO_DETAIL + message.getVideoId());
    }

    /**
     * 根据全部分P状态推进稿件：仍有转码中则等待；有失败则转码失败；全部成功则进入待审核
     */
    private void refreshVideoStatus(String videoId) {
        VideoInfo video = selectVideo(videoId);
        if (video == null) {
            return;
        }
        if (!VideoStatusEnum.TRANSCODING.getStatus().equals(video.getStatus())
                && !VideoStatusEnum.TRANSCODE_FAILED.getStatus().equals(video.getStatus())) {
            return;
        }
        List<VideoFile> files = videoFileMapper.selectListByVideoId(videoId);
        boolean transcoding = false;
        boolean failed = false;
        for (VideoFile file : files) {
            if (VideoFileStatusEnum.TRANSCODING.getStatus().equals(file.getStatus())) {
                transcoding = true;
            } else if (VideoFileStatusEnum.FAILED.getStatus().equals(file.getStatus())) {
                failed = true;
            }
        }
        if (transcoding) {
            return;
        }
        VideoInfo update = new VideoInfo();
        if (failed) {
            update.setStatus(VideoStatusEnum.TRANSCODE_FAILED.getStatus());
        } else {
            update.setStatus(VideoStatusEnum.PENDING_AUDIT.getStatus());
            if (!files.isEmpty() && files.get(0).getDuration() != null) {
                update.setDuration(files.get(0).getDuration());
            }
        }
        VideoInfoQuery query = new VideoInfoQuery();
        query.setVideoId(videoId);
        videoInfoMapper.updateByParam(update, query);
        logger.info("稿件状态更新, videoId={}, status={}", videoId, update.getStatus());
    }

    private VideoFile selectFile(String fileId) {
        VideoFileQuery query = new VideoFileQuery();
        query.setFileId(fileId);
        List<VideoFile> list = videoFileMapper.selectList(query);
        return list.isEmpty() ? null : list.get(0);
    }

    private VideoInfo selectVideo(String videoId) {
        VideoInfoQuery query = new VideoInfoQuery();
        query.setVideoId(videoId);
        List<VideoInfo> list = videoInfoMapper.selectList(query);
        return list.isEmpty() ? null : list.get(0);
    }
}
