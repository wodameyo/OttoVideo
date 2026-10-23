package com.otto.media.entity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 秒传缓存：文件 hash -> 已完成的转码产物（E 阶段转码成功后写入）
 */
@Data
public class UploadHashCacheDTO implements Serializable {

    private String hash;

    private String hlsPath;

    private String coverPath;

    private Integer duration;

    private String resolution;

    private Long fileSize;
}
