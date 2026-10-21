package com.otto.video.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 稿件详情
 */
@Data
public class VideoDetailVO implements Serializable {

    private String videoId;

    private String userId;

    /** UP 主昵称（Feign 补全） */
    private String upNickName;

    /** UP 主头像（Feign 补全） */
    private String upAvatar;

    private Integer partitionId;

    private String partitionName;

    private String title;

    private String cover;

    private String introduction;

    private List<String> tags;

    private Integer duration;

    private Integer status;

    private String rejectReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date publishTime;

    /** 所属合集 */
    private String seriesId;

    private String seriesName;

    /** 最近一次审核意见 */
    private String auditOpinion;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date auditTime;

    private List<VideoFileVO> files;
}
