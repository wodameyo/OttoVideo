package com.otto.video.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 审核列表项（供管理后台展示）
 */
@Data
public class VideoAuditItemDTO implements Serializable {

    private String videoId;

    private String userId;

    private Integer partitionId;

    private String title;

    private String cover;

    private String introduction;

    private Integer duration;

    private Integer status;

    private String rejectReason;

    private Date createTime;

    private Date publishTime;
}
