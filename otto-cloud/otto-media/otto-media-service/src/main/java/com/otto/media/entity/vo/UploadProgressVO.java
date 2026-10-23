package com.otto.media.entity.vo;

import lombok.Data;

import java.util.List;

/**
 * 上传进度
 */
@Data
public class UploadProgressVO {

    private String uploadId;

    /**
     * 0 上传中 1 合并中 2 已完成 3 已取消
     */
    private Integer status;

    private Integer chunkTotal;

    private List<Integer> uploadedChunks;
}
