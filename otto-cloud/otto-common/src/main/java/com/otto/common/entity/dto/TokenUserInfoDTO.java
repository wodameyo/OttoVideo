package com.otto.common.entity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录态用户信息（存 Redis，网关解析后透传下游）
 */
@Data
public class TokenUserInfoDTO implements Serializable {

    private String token;

    private String userId;

    private String nickName;

    private String avatar;

    /**
     * 0 普通用户 1 管理员
     */
    private Integer role;
}
