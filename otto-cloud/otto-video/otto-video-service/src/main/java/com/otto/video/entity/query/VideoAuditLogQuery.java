package com.otto.video.entity.query;

import com.otto.common.entity.query.BaseParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class VideoAuditLogQuery extends BaseParam {

    private Long id;

    private String videoId;

    private String auditorId;

    private Integer status;
}
