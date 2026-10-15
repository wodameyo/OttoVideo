package com.otto.user.entity.query;

import com.otto.common.entity.query.BaseParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserFollowQuery extends BaseParam {

    private Long id;

    private String userId;

    private String followUserId;

    /** 关注/粉丝列表中按昵称过滤 */
    private String nickNameFuzzy;
}
