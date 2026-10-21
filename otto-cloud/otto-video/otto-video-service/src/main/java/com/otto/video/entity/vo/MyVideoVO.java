package com.otto.video.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 我的稿件列表项
 */
@Data
public class MyVideoVO implements Serializable {

    private String videoId;

    private String title;

    private String cover;

    private Integer partitionId;

    private Integer status;

    private String rejectReason;

    private Integer duration;

    private Integer fileCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date publishTime;
}
