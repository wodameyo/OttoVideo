package com.otto.user.service;

public interface EmailCodeService {

    /**
     * 发送邮箱验证码
     *
     * @param email        邮箱
     * @param type         0 注册 1 登录 2 找回密码
     * @param checkCodeKey 图形验证码 key
     * @param checkCode    图形验证码
     */
    void sendEmailCode(String email, Integer type, String checkCodeKey, String checkCode);
}
