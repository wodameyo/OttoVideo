package com.otto.video.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 视频稿件
 */
@Data
public class VideoInfo implements Serializable {

    /** 视频ID（BV 风格） */
    private String videoId;

    /** 投稿用户ID */
    private String userId;

    /** 分区ID */
    private Integer partitionId;

    /** 标题 */
    private String title;

    /** 封面地址 */
    private String cover;

    /** 简介 */
    private String introduction;

    /** 标签名称（逗号分隔，冗余展示） */
    private String tags;

    /** 0转码中 1转码失败 2待审核 3已发布 4审核驳回 5已下架 */
    private Integer status;

    /** 驳回原因 */
    private String rejectReason;

    /** 总时长（秒，取第一P） */
    private Integer duration;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date publishTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
