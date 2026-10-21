package com.otto.video.mappers;

import com.otto.common.mappers.BaseMapper;
import com.otto.video.entity.po.VideoFile;
import com.otto.video.entity.query.VideoFileQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface VideoFileMapper extends BaseMapper<VideoFile, VideoFileQuery> {

    /**
     * 查询某个稿件的全部分P（按 file_index 升序）
     */
    List<VideoFile> selectListByVideoId(@Param("videoId") String videoId);

    /**
     * 批量查询多个稿件的分P
     */
    List<VideoFile> selectListByVideoIds(@Param("videoIds") List<String> videoIds);
}
