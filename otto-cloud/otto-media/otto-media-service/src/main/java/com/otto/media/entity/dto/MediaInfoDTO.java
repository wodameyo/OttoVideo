package com.otto.media.entity.dto;

import lombok.Data;

/**
 * 媒体探测信息
 */
@Data
public class MediaInfoDTO {

    private Integer width;

    private Integer height;

    /**
     * 时长（秒）
     */
    private Integer duration;

    public String getResolution() {
        return width + "x" + height;
    }
}
