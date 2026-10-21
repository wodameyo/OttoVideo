package com.otto.video.entity.query;

import com.otto.common.entity.query.BaseParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class VideoInfoQuery extends BaseParam {

    private String videoId;

    private String userId;

    private Integer partitionId;

    /** 一级分区下的所有子分区ID（与 partitionId 二选一） */
    private List<Integer> partitionIds;

    private String title;

    private String titleFuzzy;

    /** 标题/标签模糊搜索（首页搜索兜底） */
    private String keyword;

    private String tagsFuzzy;

    private Integer status;

    /** 多状态查询（审核列表等场景） */
    private List<Integer> statusList;

    private String createTimeStart;

    private String createTimeEnd;

    private String publishTimeStart;

    private String publishTimeEnd;
}
