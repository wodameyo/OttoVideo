package com.otto.video.mappers;

import com.otto.common.mappers.BaseMapper;
import com.otto.video.entity.po.VideoTag;
import com.otto.video.entity.query.VideoTagQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface VideoTagMapper extends BaseMapper<VideoTag, VideoTagQuery> {

    /**
     * 按标签名集合批量查询
     */
    List<VideoTag> selectListByNames(@Param("tagNames") List<String> tagNames);

    /**
     * 按标签 ID 集合批量查询
     */
    List<VideoTag> selectListByTagIds(@Param("tagIds") List<Long> tagIds);

    /**
     * 引用次数增减，最低为 0
     */
    Integer updateUseCount(@Param("tagId") Long tagId, @Param("delta") Integer delta);

    /**
     * 热门标签
     */
    List<VideoTag> selectHotTags(@Param("limit") Integer limit);
}
