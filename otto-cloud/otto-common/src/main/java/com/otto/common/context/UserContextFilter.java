package com.otto.common.context;

import com.otto.common.constants.Constants;
import com.otto.common.entity.dto.TokenUserInfoDTO;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * 解析网关注入的用户请求头，写入 ThreadLocal。
 * 仅 Servlet 服务（user/video/...）生效；网关是 WebFlux 不会扫描本过滤器。
 */
@Component
@Order(1)
public class UserContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String userId = httpRequest.getHeader(Constants.HEADER_USER_ID);
            if (userId != null && !userId.isBlank()) {
                TokenUserInfoDTO user = new TokenUserInfoDTO();
                user.setUserId(userId);
                user.setNickName(decode(httpRequest.getHeader(Constants.HEADER_NICK_NAME)));
                user.setAvatar(decode(httpRequest.getHeader(Constants.HEADER_AVATAR)));
                String role = httpRequest.getHeader(Constants.HEADER_ROLE);
                user.setRole(role == null ? 0 : Integer.valueOf(role));
                user.setToken(httpRequest.getHeader(Constants.HEADER_TOKEN));
                UserContextHolder.set(user);
            }
            chain.doFilter(request, response);
        } finally {
            UserContextHolder.clear();
        }
    }

    private String decode(String value) {
        return value == null ? null : URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
