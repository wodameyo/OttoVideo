package com.otto.media.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinIO 配置（otto.minio.*）
 */
@Data
@Component
@ConfigurationProperties(prefix = "otto.minio")
public class MinioProperties {

    private String endpoint;

    private String accessKey;

    private String secretKey;

    /**
     * 对外可访问地址（与 endpoint 可能不同，如经过网关/反代）
     */
    private String publicUrl;

    private String videoBucket;

    private String coverBucket;
}
