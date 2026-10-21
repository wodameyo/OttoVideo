package com.otto.video.entity.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 分P信息（详情/播放用）
 */
@Data
public class VideoFileVO implements Serializable {

    private String fileId;

    private String videoId;

    private Integer fileIndex;

    private String title;

    private Integer duration;

    private String resolution;

    private String hlsPath;

    private String coverPath;

    /** 可直接播放的 m3u8 地址 */
    private String playUrl;

    /** 可直接访问的封面地址 */
    private String coverUrl;

    /** 0转码中 1成功 2失败 */
    private Integer status;
}
