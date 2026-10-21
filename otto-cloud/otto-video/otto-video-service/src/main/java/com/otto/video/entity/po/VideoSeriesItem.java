package com.otto.video.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 合集条目
 */
@Data
public class VideoSeriesItem implements Serializable {

    private Long id;

    private String seriesId;

    private String videoId;

    private Integer sort;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
