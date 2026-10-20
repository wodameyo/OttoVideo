package com.otto.video.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 审核请求参数：status=3 通过，status=4 驳回
 */
@Data
public class VideoAuditParamDTO implements Serializable {

    private String videoId;

    private Integer status;

    private String opinion;

    /** 审核人ID（管理后台调用时传入） */
    private String auditorId;
}
