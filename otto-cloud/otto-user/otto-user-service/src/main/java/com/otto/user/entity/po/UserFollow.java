package com.otto.user.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户关注关系
 */
@Data
public class UserFollow implements Serializable {

    private Long id;

    /** 粉丝用户ID */
    private String userId;

    /** 被关注用户ID */
    private String followUserId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
