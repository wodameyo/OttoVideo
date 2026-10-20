package com.otto.video.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 视频分P文件基础信息（上传前归属校验、转码结果回写用）
 */
@Data
public class VideoFileDTO implements Serializable {

    private String fileId;

    private String videoId;

    private Integer fileIndex;

    private String title;

    private Integer duration;

    private Long fileSize;

    private String resolution;

    private String originPath;

    private String hlsPath;

    private String coverPath;

    /** 0转码中 1成功 2失败 */
    private Integer status;
}
