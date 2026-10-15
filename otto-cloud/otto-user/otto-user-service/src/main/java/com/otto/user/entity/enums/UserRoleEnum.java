package com.otto.user.entity.enums;

import lombok.Getter;

@Getter
public enum UserRoleEnum {

    USER(0, "普通用户"),
    ADMIN(1, "管理员");

    private final Integer role;
    private final String desc;

    UserRoleEnum(Integer role, String desc) {
        this.role = role;
        this.desc = desc;
    }
}
