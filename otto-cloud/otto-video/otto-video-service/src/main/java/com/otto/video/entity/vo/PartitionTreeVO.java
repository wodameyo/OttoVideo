package com.otto.video.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分区树节点
 */
@Data
public class PartitionTreeVO implements Serializable {

    private Integer partitionId;

    private Integer parentId;

    private String name;

    private String code;

    private String icon;

    private Integer sort;

    private List<PartitionTreeVO> children;
}
