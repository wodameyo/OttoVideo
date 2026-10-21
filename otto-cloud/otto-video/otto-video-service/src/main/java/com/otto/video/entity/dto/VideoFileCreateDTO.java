package com.otto.video.entity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建投稿时的单个分P
 */
@Data
public class VideoFileCreateDTO implements Serializable {

    /** 分P标题，为空时默认 P1/P2... */
    private String title;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 分辨率，如 1080P，可为空 */
    private String resolution;
}
