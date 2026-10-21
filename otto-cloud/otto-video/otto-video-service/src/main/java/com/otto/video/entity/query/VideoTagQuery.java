package com.otto.video.entity.query;

import com.otto.common.entity.query.BaseParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class VideoTagQuery extends BaseParam {

    private Long tagId;

    private String tagName;

    private String tagNameFuzzy;
}
