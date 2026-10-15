package com.otto.common.entity.vo;

import lombok.Data;

import java.util.List;

/**
 * 分页返回结果
 */
@Data
public class PaginationResultVO<T> {

    private Integer pageNo;

    private Integer pageSize;

    private Integer totalCount;

    private Integer pageTotal;

    private List<T> list;

    public PaginationResultVO() {
    }

    public PaginationResultVO(Integer totalCount, Integer pageSize, Integer pageNo, List<T> list) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
        this.pageTotal = pageSize == 0 ? 0 : (totalCount + pageSize - 1) / pageSize;
        this.list = list;
    }
}
