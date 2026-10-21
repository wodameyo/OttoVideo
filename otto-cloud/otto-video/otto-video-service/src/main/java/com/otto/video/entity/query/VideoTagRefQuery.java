package com.otto.video.entity.query;

import com.otto.common.entity.query.BaseParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class VideoTagRefQuery extends BaseParam {

    private Long id;

    private String videoId;

    private Long tagId;
}
