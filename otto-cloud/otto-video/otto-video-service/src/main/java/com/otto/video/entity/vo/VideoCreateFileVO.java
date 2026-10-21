package com.otto.video.entity.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 投稿创建后返回的分P标识
 */
@Data
public class VideoCreateFileVO implements Serializable {

    private String fileId;

    private Integer fileIndex;

    private String title;
}
