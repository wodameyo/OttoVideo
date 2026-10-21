package com.otto.video.entity.query;

import com.otto.common.entity.query.BaseParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class VideoSeriesQuery extends BaseParam {

    private String seriesId;

    private String userId;

    private String name;

    private String nameFuzzy;
}
