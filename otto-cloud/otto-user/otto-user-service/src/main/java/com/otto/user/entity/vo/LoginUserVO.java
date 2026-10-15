package com.otto.user.entity.vo;

import com.otto.user.entity.po.UserInfo;
import lombok.Data;

/**
 * 登录成功返回
 */
@Data
public class LoginUserVO {

    private String token;

    private UserInfo userInfo;
}
