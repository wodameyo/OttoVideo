package com.otto.video.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 视频审核记录
 */
@Data
public class VideoAuditLog implements Serializable {

    private Long id;

    private String videoId;

    /** 审核人ID */
    private String auditorId;

    /** 审核结果 3通过 4驳回 */
    private Integer status;

    /** 审核意见 */
    private String opinion;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
