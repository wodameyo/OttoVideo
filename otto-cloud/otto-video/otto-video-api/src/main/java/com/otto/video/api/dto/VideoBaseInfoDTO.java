package com.otto.video.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 视频稿件基础信息（跨服务展示用）
 */
@Data
public class VideoBaseInfoDTO implements Serializable {

    private String videoId;

    private String userId;

    private Integer partitionId;

    private String title;

    private String cover;

    private Integer duration;

    /** 0转码中 1转码失败 2待审核 3已发布 4审核驳回 5已下架 */
    private Integer status;

    private String tags;

    private Date publishTime;

    private Date createTime;
}
