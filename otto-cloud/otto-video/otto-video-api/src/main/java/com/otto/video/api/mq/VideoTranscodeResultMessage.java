package com.otto.video.api.mq;

import lombok.Data;

import java.io.Serializable;

/**
 * 转码结果消息：media 转码完成后投递，video-service 消费并推进状态机
 */
@Data
public class VideoTranscodeResultMessage implements Serializable {

    private String videoId;

    private String fileId;

    /** 1成功 2失败 */
    private Integer status;

    private Integer duration;

    private String hlsPath;

    private String coverPath;

    private String resolution;

    private Long fileSize;

    private String errorMsg;
}
