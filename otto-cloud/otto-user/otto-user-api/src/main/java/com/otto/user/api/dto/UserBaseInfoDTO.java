package com.otto.user.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户基础信息（跨服务展示用：昵称/头像/签名）
 */
@Data
public class UserBaseInfoDTO implements Serializable {

    private String userId;

    private String nickName;

    private String avatar;

    private String personalSign;
}
