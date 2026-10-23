package com.otto.media.entity.vo;

import lombok.Data;

import java.util.List;

/**
 * 上传初始化结果
 */
@Data
public class UploadInitVO {

    private String videoId;

    private String fileId;

    private String uploadId;

    /**
     * 命中秒传：无需再上传分片
     */
    private Boolean instant;

    private Integer chunkSize;

    private Integer chunkTotal;

    /**
     * 已上传的分片序号（断点续传）
     */
    private List<Integer> uploadedChunks;

    private String hlsPath;

    private String coverPath;

    private Integer duration;

    private String resolution;

    private Long fileSize;
}
