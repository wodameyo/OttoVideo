package com.otto.video.entity.query;

import com.otto.common.entity.query.BaseParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class VideoPartitionQuery extends BaseParam {

    private Integer partitionId;

    private Integer parentId;

    private String name;

    private String nameFuzzy;

    private String code;

    private Integer status;
}
