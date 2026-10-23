package com.otto.media.service;

import com.otto.media.entity.dto.UploadInitDTO;
import com.otto.media.entity.vo.UploadCompleteVO;
import com.otto.media.entity.vo.UploadInitVO;
import com.otto.media.entity.vo.UploadProgressVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 分片上传 / 断点续传 / 秒传
 */
public interface UploadService {

    /**
     * 初始化上传（校验稿件归属，命中 hash 直接秒传）
     */
    UploadInitVO init(UploadInitDTO dto, String userId);

    /**
     * 上传单个分片
     */
    UploadProgressVO uploadChunk(String uploadId, Integer index, MultipartFile file, String userId);

    /**
     * 合并分片、上传原片并投递转码消息
     */
    UploadCompleteVO complete(String uploadId, String userId);

    /**
     * 查询上传进度（断点续传）
     */
    UploadProgressVO status(String uploadId, String userId);

    /**
     * 取消上传并清理临时文件
     */
    void cancel(String uploadId, String userId);
}
