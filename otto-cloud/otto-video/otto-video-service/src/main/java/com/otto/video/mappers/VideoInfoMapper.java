package com.otto.video.mappers;

import com.otto.common.mappers.BaseMapper;
import com.otto.video.entity.po.VideoInfo;
import com.otto.video.entity.query.VideoInfoQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface VideoInfoMapper extends BaseMapper<VideoInfo, VideoInfoQuery> {

    /**
     * 按视频 ID 集合批量查询
     */
    List<VideoInfo> selectListByVideoIds(@Param("videoIds") List<String> videoIds);
}
