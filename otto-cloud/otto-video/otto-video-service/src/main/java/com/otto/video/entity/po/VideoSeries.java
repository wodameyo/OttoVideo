package com.otto.video.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 视频合集
 */
@Data
public class VideoSeries implements Serializable {

    private String seriesId;

    /** UP 主ID */
    private String userId;

    /** 合集名称 */
    private String name;

    private String description;

    private String cover;

    private Integer videoCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
