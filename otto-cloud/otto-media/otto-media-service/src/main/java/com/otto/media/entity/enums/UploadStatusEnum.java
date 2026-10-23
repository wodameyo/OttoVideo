package com.otto.media.entity.enums;

import lombok.Getter;

/**
 * 上传会话状态
 */
@Getter
public enum UploadStatusEnum {

    UPLOADING(0, "上传中"),
    MERGING(1, "合并中"),
    COMPLETED(2, "已完成"),
    CANCELED(3, "已取消");

    private final Integer code;

    private final String desc;

    UploadStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
