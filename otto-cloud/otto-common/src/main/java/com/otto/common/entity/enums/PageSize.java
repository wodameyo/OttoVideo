package com.otto.common.entity.enums;

import lombok.Getter;

/**
 * 分页大小枚举（沿用 betterMusic 风格）
 */
@Getter
public enum PageSize {

    SIZE10(10, 10),
    SIZE15(15, 15),
    SIZE20(20, 20),
    SIZE30(30, 30),
    SIZE40(40, 40),
    SIZE50(50, 50);

    private final int size;
    private final int desc;

    PageSize(int size, int desc) {
        this.size = size;
        this.desc = desc;
    }
}
