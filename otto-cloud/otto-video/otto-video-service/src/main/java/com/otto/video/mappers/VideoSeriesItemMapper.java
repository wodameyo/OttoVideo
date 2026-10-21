package com.otto.video.mappers;

import com.otto.common.mappers.BaseMapper;
import com.otto.video.entity.po.VideoSeriesItem;
import com.otto.video.entity.query.VideoSeriesItemQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface VideoSeriesItemMapper extends BaseMapper<VideoSeriesItem, VideoSeriesItemQuery> {

    /**
     * 查询合集条目（按 sort 升序）
     */
    List<VideoSeriesItem> selectListBySeriesId(@Param("seriesId") String seriesId);

    /**
     * 合集当前最大排序值
     */
    Integer selectMaxSort(@Param("seriesId") String seriesId);
}
