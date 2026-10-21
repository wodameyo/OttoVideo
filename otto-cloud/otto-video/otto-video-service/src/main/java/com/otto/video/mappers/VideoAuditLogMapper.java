package com.otto.video.mappers;

import com.otto.common.mappers.BaseMapper;
import com.otto.video.entity.po.VideoAuditLog;
import com.otto.video.entity.query.VideoAuditLogQuery;
import org.apache.ibatis.annotations.Param;

public interface VideoAuditLogMapper extends BaseMapper<VideoAuditLog, VideoAuditLogQuery> {

    /**
     * 查询稿件最近一次审核记录
     */
    VideoAuditLog selectLatestByVideoId(@Param("videoId") String videoId);
}
