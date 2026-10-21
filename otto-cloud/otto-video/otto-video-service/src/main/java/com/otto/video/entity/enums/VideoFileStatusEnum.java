package com.otto.video.entity.enums;

import lombok.Getter;

/**
 * 分P文件状态：0转码中 1成功 2失败
 */
@Getter
public enum VideoFileStatusEnum {

    TRANSCODING(0, "转码中"),
    SUCCESS(1, "转码成功"),
    FAILED(2, "转码失败");

    private final Integer status;
    private final String desc;

    VideoFileStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}
