package com.otto.media.service.impl;

import com.otto.common.exception.BusinessException;
import com.otto.media.config.MinioProperties;
import com.otto.media.service.StorageService;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MinIO 对象存储实现
 */
@Service
public class StorageServiceImpl implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(StorageServiceImpl.class);

    private static final long PART_SIZE = 10L * 1024 * 1024;

    @Resource
    private MinioClient minioClient;

    @Resource
    private MinioProperties minioProperties;

    /**
     * 已确认存在的 bucket，避免每次上传都查询
     */
    private final Set<String> ensuredBuckets = ConcurrentHashMap.newKeySet();

    @Override
    public void ensureBucket(String bucket) {
        if (ensuredBuckets.contains(bucket)) {
            return;
        }
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                logger.info("创建存储桶：{}", bucket);
            }
            ensuredBuckets.add(bucket);
        } catch (Exception e) {
            throw new BusinessException("初始化存储桶失败：" + bucket, e);
        }
    }

    @Override
    public void uploadFile(String bucket, String objectName, Path file, String contentType) {
        try {
            long size = Files.size(file);
            try (InputStream inputStream = Files.newInputStream(file)) {
                uploadStream(bucket, objectName, inputStream, size, contentType);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("读取待上传文件失败：" + file, e);
        }
    }

    @Override
    public void uploadStream(String bucket, String objectName, InputStream inputStream, long size, String contentType) {
        ensureBucket(bucket);
        try {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(inputStream, size, size > PART_SIZE ? PART_SIZE : -1)
                    .contentType(contentType == null ? "application/octet-stream" : contentType)
                    .build();
            minioClient.putObject(args);
            logger.info("对象上传成功：{}/{}，大小 {} 字节", bucket, objectName, size);
        } catch (Exception e) {
            throw new BusinessException("上传对象存储失败：" + objectName, e);
        }
    }

    @Override
    public boolean exists(String bucket, String objectName) {
        return statObjectSize(bucket, objectName) != null;
    }

    @Override
    public Long statObjectSize(String bucket, String objectName) {
        try {
            return minioClient.statObject(StatObjectArgs.builder().bucket(bucket).object(objectName).build()).size();
        } catch (ErrorResponseException e) {
            String code = e.errorResponse() == null ? null : e.errorResponse().code();
            if ("NoSuchKey".equals(code) || "NoSuchBucket".equals(code)) {
                return null;
            }
            throw new BusinessException("查询对象信息失败：" + objectName, e);
        } catch (Exception e) {
            throw new BusinessException("查询对象信息失败：" + objectName, e);
        }
    }

    @Override
    public void removeObject(String bucket, String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectName).build());
        } catch (Exception e) {
            throw new BusinessException("删除对象失败：" + objectName, e);
        }
    }

    @Override
    public List<String> listObjects(String bucket, String prefix) {
        ensureBucket(bucket);
        List<String> result = new ArrayList<>();
        try {
            Iterable<io.minio.Result<io.minio.messages.Item>> items = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucket).prefix(prefix).recursive(true).build());
            for (io.minio.Result<io.minio.messages.Item> item : items) {
                result.add(item.get().objectName());
            }
            return result;
        } catch (Exception e) {
            throw new BusinessException("列举对象失败：" + prefix, e);
        }
    }

    @Override
    public String getPublicUrl(String bucket, String objectName) {
        String base = minioProperties.getPublicUrl();
        if (base == null || base.isBlank()) {
            base = minioProperties.getEndpoint();
        }
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/" + bucket + "/" + objectName;
    }

    @Override
    public void downloadToFile(String bucket, String objectName, Path target) {
        try {
            Files.createDirectories(target.getParent());
            try (InputStream inputStream = minioClient.getObject(
                    GetObjectArgs.builder().bucket(bucket).object(objectName).build())) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            throw new BusinessException("下载对象失败：" + objectName, e);
        }
    }
}
