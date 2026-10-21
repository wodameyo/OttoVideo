package com.otto.video.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 投稿创建结果：前端据此逐个分P上传
 */
@Data
public class VideoCreateResultVO implements Serializable {

    private String videoId;

    private List<VideoCreateFileVO> files;
}
