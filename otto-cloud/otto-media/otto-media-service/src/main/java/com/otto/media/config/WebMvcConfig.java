package com.otto.media.config;

import com.otto.media.interceptor.LoginInterceptor;
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
        // 服务内部接口、转码回调不拦截
        registry.addInterceptor(loginInterceptor)
                .excludePathPatterns("/inner/**")
                .addPathPatterns("/**");
    }
}
