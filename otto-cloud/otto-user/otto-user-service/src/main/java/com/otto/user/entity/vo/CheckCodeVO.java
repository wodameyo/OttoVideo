package com.otto.user.entity.vo;

import lombok.Data;

/**
 * 图形验证码
 */
@Data
public class CheckCodeVO {

    /** 验证码 key（登录/注册时回传） */
    private String checkCodeKey;

    /** base64 图片 */
    private String checkCode;
}
