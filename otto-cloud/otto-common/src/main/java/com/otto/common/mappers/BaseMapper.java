package com.otto.common.mappers;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通用 Mapper 基类（沿用 betterMusic 自研风格，XML 中提供对应语句）
 */
public interface BaseMapper<T, P> {

    /**
     * 根据参数查询集合
     */
    List<T> selectList(@Param("query") P p);

    /**
     * 根据参数查询数量
     */
    Integer selectCount(@Param("query") P p);

    /**
     * 插入
     */
    Integer insert(@Param("bean") T t);

    /**
     * 插入或者更新
     */
    Integer insertOrUpdate(@Param("bean") T t);

    /**
     * 批量插入
     */
    Integer insertBatch(@Param("list") List<T> list);

    /**
     * 批量插入或更新
     */
    Integer insertOrUpdateBatch(@Param("list") List<T> list);

    /**
     * 多条件更新
     */
    Integer updateByParam(@Param("bean") T t, @Param("query") P p);

    /**
     * 多条件删除
     */
    Integer deleteByParam(@Param("query") P p);
}
