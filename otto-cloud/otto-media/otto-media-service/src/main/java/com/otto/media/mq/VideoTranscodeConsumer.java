package com.otto.media.mq;

import com.otto.common.constants.Constants;
import com.otto.media.service.TranscodeService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 转码请求消费者：使用独立线程池容器，异常内部消化避免无限重试
 */
@Component
public class VideoTranscodeConsumer {

    private static final Logger logger = LoggerFactory.getLogger(VideoTranscodeConsumer.class);

    @Resource
    private TranscodeService transcodeService;

    @RabbitListener(queues = Constants.MQ_QUEUE_TRANSCODE, containerFactory = "transcodeContainerFactory")
    public void onTranscode(VideoTranscodeMessage message) {
        try {
            transcodeService.transcode(message);
        } catch (Exception e) {
            logger.error("转码消费异常，丢弃消息：fileId={}", message == null ? null : message.getFileId(), e);
        }
    }
}
