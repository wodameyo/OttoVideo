package com.otto.user.service;

import com.otto.common.exception.BusinessException;
import com.otto.common.utils.StringTools;
import com.otto.user.config.MinioProperties;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.Resource;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class FileService {

    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024L;

    @Resource
    private MinioClient minioClient;

    @Resource
    private MinioProperties minioProperties;

    /**
     * 上传头像，返回可访问 URL
     */
    public String uploadAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new BusinessException("头像不能超过 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("只能上传图片文件");
        }
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        // yyyy-MM 分目录，沿用旧工程按月分目录习惯
        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String objectKey = month + "/" + StringTools.getUUID() + (StringTools.isEmpty(ext) ? ".png" : "." + ext);
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getAvatarBucket())
                    .object(objectKey)
                    .contentType(contentType)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .build());
        } catch (Exception e) {
            throw new BusinessException("头像上传失败:" + e.getMessage());
        }
        return minioProperties.getPublicUrl() + "/" + minioProperties.getAvatarBucket() + "/" + objectKey;
    }
}
