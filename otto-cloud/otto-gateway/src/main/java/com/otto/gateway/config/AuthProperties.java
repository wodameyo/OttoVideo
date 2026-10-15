package com.otto.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关鉴权配置（白名单 / 管理员路径）
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "otto.auth")
public class AuthProperties {

    private List<String> whitePaths = new ArrayList<>();

    private List<String> adminPaths = new ArrayList<>();
}
