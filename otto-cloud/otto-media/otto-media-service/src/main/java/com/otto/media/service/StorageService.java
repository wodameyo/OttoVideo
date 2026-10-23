package com.otto.media.service;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * 对象存储能力（MinIO）
 */
public interface StorageService {

    /**
     * 确保 bucket 存在（不存在则创建）
     */
    void ensureBucket(String bucket);

    /**
     * 上传本地文件
     */
    void uploadFile(String bucket, String objectName, Path file, String contentType);

    /**
     * 上传流
     */
    void uploadStream(String bucket, String objectName, InputStream inputStream, long size, String contentType);

    /**
     * 判断对象是否存在
     */
    boolean exists(String bucket, String objectName);

    /**
     * 获取对象大小，不存在返回 null
     */
    Long statObjectSize(String bucket, String objectName);

    /**
     * 删除对象
     */
    void removeObject(String bucket, String objectName);

    /**
     * 按前缀递归列出对象 key
     */
    List<String> listObjects(String bucket, String prefix);

    /**
     * 拼接公网访问地址
     */
    String getPublicUrl(String bucket, String objectName);

    /**
     * 读取对象内容到本地文件
     */
    void downloadToFile(String bucket, String objectName, Path target);
}
