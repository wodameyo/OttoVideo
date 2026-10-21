package com.otto.video.entity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 编辑分P标题
 */
@Data
public class VideoFileUpdateDTO implements Serializable {

    private String fileId;

    private String title;
}
