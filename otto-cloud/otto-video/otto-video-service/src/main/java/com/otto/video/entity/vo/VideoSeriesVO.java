package com.otto.video.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 合集信息
 */
@Data
public class VideoSeriesVO implements Serializable {

    private String seriesId;

    private String userId;

    private String name;

    private String description;

    private String cover;

    private Integer videoCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
