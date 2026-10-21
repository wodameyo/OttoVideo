package com.otto.video.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 视频标签
 */
@Data
public class VideoTag implements Serializable {

    private Long tagId;

    /** 标签名 */
    private String tagName;

    /** 被引用次数 */
    private Integer useCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
