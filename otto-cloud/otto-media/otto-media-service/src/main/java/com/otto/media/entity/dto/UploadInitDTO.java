package com.otto.media.entity.dto;

import lombok.Data;

/**
 * 上传初始化参数
 */
@Data
public class UploadInitDTO {

    private String videoId;

    private String fileId;

    private String fileName;

    private Long fileSize;

    private Integer chunkTotal;

    /**
     * 文件 MD5（可选，用于秒传）
     */
    private String hash;
}
