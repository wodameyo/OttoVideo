package com.otto.video.entity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 投稿编辑参数（null 表示不修改）
 */
@Data
public class VideoEditDTO implements Serializable {

    private String videoId;

    private Integer partitionId;

    private String title;

    private String cover;

    private String introduction;

    /** 为空列表时表示清空标签；null 表示不修改 */
    private List<String> tags;

    /** 空字符串表示移出合集；null 表示不修改 */
    private String seriesId;

    /** 仅修改分P标题 */
    private List<VideoFileUpdateDTO> files;
}
