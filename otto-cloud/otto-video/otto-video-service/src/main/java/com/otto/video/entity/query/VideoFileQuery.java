package com.otto.video.entity.query;

import com.otto.common.entity.query.BaseParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class VideoFileQuery extends BaseParam {

    private String fileId;

    private String videoId;

    private Integer fileIndex;

    private Integer status;
}
