package com.otto.video.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 视频分P / 转码文件
 */
@Data
public class VideoFile implements Serializable {

    private String fileId;

    private String videoId;

    /** 第几P */
    private Integer fileIndex;

    /** 分P标题 */
    private String title;

    /** 时长（秒） */
    private Integer duration;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 分辨率，如 1080P */
    private String resolution;

    /** 原片存储路径（MinIO key） */
    private String originPath;

    /** HLS m3u8 存储路径（MinIO key） */
    private String hlsPath;

    /** 截图封面路径 */
    private String coverPath;

    /** 0转码中 1成功 2失败 */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
