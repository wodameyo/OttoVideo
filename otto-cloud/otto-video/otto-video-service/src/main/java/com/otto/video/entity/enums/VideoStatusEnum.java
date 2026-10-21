package com.otto.video.entity.enums;

import lombok.Getter;

/**
 * 稿件状态：0转码中 1转码失败 2待审核 3已发布 4审核驳回 5已下架
 */
@Getter
public enum VideoStatusEnum {

    TRANSCODING(0, "转码中"),
    TRANSCODE_FAILED(1, "转码失败"),
    PENDING_AUDIT(2, "待审核"),
    PUBLISHED(3, "已发布"),
    REJECTED(4, "审核驳回"),
    OFFLINE(5, "已下架");

    private final Integer status;
    private final String desc;

    VideoStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static VideoStatusEnum getByStatus(Integer status) {
        for (VideoStatusEnum value : values()) {
            if (value.status.equals(status)) {
                return value;
            }
        }
        return null;
    }
}
