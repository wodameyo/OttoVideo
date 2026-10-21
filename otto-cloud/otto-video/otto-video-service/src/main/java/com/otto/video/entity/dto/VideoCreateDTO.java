package com.otto.video.entity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 投稿创建参数
 */
@Data
public class VideoCreateDTO implements Serializable {

    private Integer partitionId;

    private String title;

    private String cover;

    private String introduction;

    private List<String> tags;

    /** 合集ID，可为空 */
    private String seriesId;

    /** 分P列表，至少 1 个 */
    private List<VideoFileCreateDTO> files;
}
