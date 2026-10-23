package com.otto.media.mq;

import lombok.Data;

import java.io.Serializable;

/**
 * 转码请求消息：media 上传完成后投递，转码消费者（media）处理
 */
@Data
public class VideoTranscodeMessage implements Serializable {

    private String videoId;

    private String fileId;

    private String bucket;

    /**
     * 原片对象 key
     */
    private String objectName;

    /**
     * 原始文件名（判断后缀）
     */
    private String fileName;

    private Long fileSize;

    /**
     * 文件 MD5（转码成功后写入秒传缓存）
     */
    private String hash;
}
