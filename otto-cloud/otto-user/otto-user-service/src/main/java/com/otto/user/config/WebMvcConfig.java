package com.otto.user.config;

import com.otto.user.interceptor.LoginInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 服务内部 Feign 接口不拦截
        registry.addInterceptor(loginInterceptor).excludePathPatterns("/inner/**").addPathPatterns("/**");
    }
}
