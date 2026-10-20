package com.otto.video.interceptor;

import com.otto.common.constants.Constants;
import com.otto.common.context.UserContextHolder;
import com.otto.common.entity.dto.TokenUserInfoDTO;
import com.otto.common.entity.enums.ResponseCodeEnum;
import com.otto.common.exception.BusinessException;
import com.otto.common.redis.RedisUtils;
import com.otto.video.annotation.GlobalInterceptor;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器：网关注入的用户头优先，兼容直连服务时用 token 头查 Redis
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Resource
    private RedisUtils redisUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        GlobalInterceptor annotation = handlerMethod.getMethodAnnotation(GlobalInterceptor.class);
        if (annotation == null) {
            annotation = handlerMethod.getBeanType().getAnnotation(GlobalInterceptor.class);
        }
        if (annotation == null || !annotation.checkLogin()) {
            return true;
        }
        TokenUserInfoDTO user = UserContextHolder.get();
        if (user == null) {
            String token = request.getHeader(Constants.HEADER_TOKEN);
            if (token != null && !token.isBlank()) {
                user = redisUtils.get(Constants.REDIS_KEY_TOKEN_WEB + token, TokenUserInfoDTO.class);
                if (user != null) {
                    UserContextHolder.set(user);
                }
            }
        }
        if (user == null || user.getUserId() == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        return true;
    }
}
