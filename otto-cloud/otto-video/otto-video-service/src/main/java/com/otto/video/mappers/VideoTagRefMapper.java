package com.otto.video.mappers;

import com.otto.common.mappers.BaseMapper;
import com.otto.video.entity.po.VideoTagRef;
import com.otto.video.entity.query.VideoTagRefQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface VideoTagRefMapper extends BaseMapper<VideoTagRef, VideoTagRefQuery> {

    /**
     * 查询稿件关联的标签 ID
     */
    List<Long> selectTagIdListByVideoId(@Param("videoId") String videoId);

    /**
     * 删除稿件全部标签关联
     */
    Integer deleteByVideoId(@Param("videoId") String videoId);
}
