package com.otto.user.entity.query;

import com.otto.common.entity.query.BaseParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserInfoQuery extends BaseParam {

    private String userId;
    private String userIdFuzzy;

    private String email;
    private String emailFuzzy;

    private String nickName;
    private String nickNameFuzzy;

    private Integer sex;
    private Integer role;
    private Integer status;

    private String createTimeStart;
    private String createTimeEnd;
}
