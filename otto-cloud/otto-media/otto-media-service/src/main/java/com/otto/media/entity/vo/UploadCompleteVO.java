package com.otto.media.entity.vo;

import lombok.Data;

/**
 * 上传完成结果
 */
@Data
public class UploadCompleteVO {

    private String videoId;

    private String fileId;

    /**
     * 原片 MinIO key
     */
    private String originPath;

    private Long fileSize;
}
