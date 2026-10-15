package com.otto.common.entity.vo;

import lombok.Data;

/**
 * 统一响应结构（沿用 betterMusic 风格：status / code / info / data）
 */
@Data
public class ResponseVO<T> {

    private String status;

    private Integer code;

    private String info;

    private T data;
}
