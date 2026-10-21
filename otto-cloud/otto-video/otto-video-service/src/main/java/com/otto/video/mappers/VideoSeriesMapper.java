package com.otto.video.mappers;

import com.otto.common.mappers.BaseMapper;
import com.otto.video.entity.po.VideoSeries;
import com.otto.video.entity.query.VideoSeriesQuery;
import org.apache.ibatis.annotations.Param;

public interface VideoSeriesMapper extends BaseMapper<VideoSeries, VideoSeriesQuery> {

    /**
     * 合集视频数增减，最低为 0
     */
    Integer updateVideoCount(@Param("seriesId") String seriesId, @Param("delta") Integer delta);
}
