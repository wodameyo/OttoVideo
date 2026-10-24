package com.otto.media.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 转码配置（otto.transcode.*）
 */
@Data
@Component
@ConfigurationProperties(prefix = "otto.transcode")
public class TranscodeProperties {

    /**
     * 转码工作目录根路径
     */
    private String workDir;

    /**
     * 消费线程池核心线程数
     */
    private Integer coreSize = 2;

    /**
     * 消费线程池最大线程数
     */
    private Integer maxSize = 4;

    /**
     * 消费线程池队列容量
     */
    private Integer queueCapacity = 50;

    /**
     * 转码成功后是否保留 MinIO 原片
     */
    private Boolean keepOrigin = false;

    /**
     * 转码任务幂等 key 有效期（天）
     */
    private Integer taskExpireDays = 7;
}
