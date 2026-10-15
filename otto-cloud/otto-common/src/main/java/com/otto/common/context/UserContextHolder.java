package com.otto.common.context;

import com.otto.common.entity.dto.TokenUserInfoDTO;

/**
 * 当前登录用户上下文（网关注入的请求头由 UserContextFilter 解析）
 */
public class UserContextHolder {

    private static final ThreadLocal<TokenUserInfoDTO> CONTEXT = new ThreadLocal<>();

    public static void set(TokenUserInfoDTO user) {
        CONTEXT.set(user);
    }

    public static TokenUserInfoDTO get() {
        return CONTEXT.get();
    }

    public static String getUserId() {
        TokenUserInfoDTO user = CONTEXT.get();
        return user == null ? null : user.getUserId();
    }

    public static boolean isAdmin() {
        TokenUserInfoDTO user = CONTEXT.get();
        return user != null && Integer.valueOf(1).equals(user.getRole());
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
