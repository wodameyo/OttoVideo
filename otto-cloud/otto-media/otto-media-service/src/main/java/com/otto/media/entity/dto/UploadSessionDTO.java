package com.otto.media.entity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 上传会话（Redis：otto:upload:session:{uploadId}，TTL 1 天）
 */
@Data
public class UploadSessionDTO implements Serializable {

    private String uploadId;

    private String videoId;

    private String fileId;

    private String userId;

    private String fileName;

    private Long fileSize;

    private Integer chunkTotal;

    /**
     * 客户端计算的文件 MD5（用于秒传）
     */
    private String hash;

    /**
     * 0 上传中 1 合并中 2 已完成 3 已取消
     */
    private Integer status;

    private Long createdAt;
}
