package com.otto.common.entity.query;

import lombok.Data;

/**
 * 查询参数基类（分页 + 排序，沿用 betterMusic 风格）
 */
@Data
public class BaseParam {

    private SimplePage simplePage;

    private Integer pageNo;

    private Integer pageSize;

    private String orderBy;
}
