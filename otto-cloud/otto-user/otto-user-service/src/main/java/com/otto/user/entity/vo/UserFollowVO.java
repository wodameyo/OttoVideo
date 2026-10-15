package com.otto.user.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 关注/粉丝列表项（关注关系 + 对方用户信息）
 */
@Data
public class UserFollowVO {

    private String userId;

    private String nickName;

    private String avatar;

    private String personalSign;

    private Integer totalFans;

    /** 当前登录用户是否已关注该用户 */
    private Boolean followed;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
