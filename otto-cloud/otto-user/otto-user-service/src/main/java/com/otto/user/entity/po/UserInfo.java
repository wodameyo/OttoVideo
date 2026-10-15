package com.otto.user.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户信息
 */
@Data
public class UserInfo implements Serializable {

    /** 用户ID */
    private String userId;

    /** 邮箱 */
    private String email;

    /** 昵称 */
    private String nickName;

    /** 头像 */
    private String avatar;

    /** 密码（MD5加盐） */
    @JsonIgnore
    private String password;

    /** 密码盐 */
    @JsonIgnore
    private String salt;

    /** 性别 0未知 1男 2女 */
    private Integer sex;

    /** 个性签名 */
    private String personalSign;

    /** 生日 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    /** 角色 0普通 1管理员 */
    private Integer role;

    /** 状态 0禁用 1正常 */
    private Integer status;

    /** 硬币数 */
    private Integer integral;

    private Integer totalFollows;

    private Integer totalFans;

    private Integer totalLikes;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastLoginTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
