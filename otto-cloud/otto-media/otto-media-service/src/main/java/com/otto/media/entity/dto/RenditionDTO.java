package com.otto.media.entity.dto;

import lombok.Data;

/**
 * HLS 清晰度规格
 */
@Data
public class RenditionDTO {

    /**
     * 播放列表目录名，如 720p / 480p
     */
    private String name;

    private Integer width;

    private Integer height;

    /**
     * 视频码率，如 2200k
     */
    private String videoBitrate;

    /**
     * 音频码率，如 128k
     */
    private String audioBitrate;

    /**
     * master 播放列表中的 BANDWIDTH
     */
    private Integer bandwidth;
}
