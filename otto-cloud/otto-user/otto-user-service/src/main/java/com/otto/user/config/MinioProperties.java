package com.otto.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "otto.minio")
public class MinioProperties {

    private String endpoint;

    private String accessKey;

    private String secretKey;

    /** 对外访问地址（与内网 endpoint 可不同） */
    private String publicUrl;

    private String avatarBucket;
}
