package com.otto.media.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 上传配置（otto.upload.*）
 */
@Data
@Component
@ConfigurationProperties(prefix = "otto.upload")
public class UploadProperties {

    /**
     * 分片临时目录根路径
     */
    private String tempDir;

    /**
     * 单分片大小（字节）
     */
    private Long chunkSize = 5L * 1024 * 1024;

    /**
     * 单文件最大大小（字节）
     */
    private Long maxFileSize = 2L * 1024 * 1024 * 1024;

    /**
     * 允许的文件后缀
     */
    private List<String> allowedExtensions = new ArrayList<>(List.of("mp4", "flv", "mkv", "mov"));

    /**
     * 临时目录过期时间（小时），启动时清理
     */
    private Integer tempExpireHours = 24;
}
