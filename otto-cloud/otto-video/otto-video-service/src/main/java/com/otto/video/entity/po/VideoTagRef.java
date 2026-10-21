package com.otto.video.entity.po;

import lombok.Data;

import java.io.Serializable;

/**
 * 稿件-标签关联
 */
@Data
public class VideoTagRef implements Serializable {

    private Long id;

    private String videoId;

    private Long tagId;
}
