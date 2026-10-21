package com.otto.video.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 视频分区（两级）
 */
@Data
public class VideoPartition implements Serializable {

    /** 分区ID */
    private Integer partitionId;

    /** 父分区ID，0 为一级分区 */
    private Integer parentId;

    /** 分区名称 */
    private String name;

    /** 分区编码 */
    private String code;

    /** 分区图标 */
    private String icon;

    /** 排序 */
    private Integer sort;

    /** 状态 0停用 1启用 */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
