package com.otto.user.mappers;

import com.otto.common.mappers.BaseMapper;
import com.otto.user.entity.po.UserFollow;
import com.otto.user.entity.query.UserFollowQuery;
import com.otto.user.entity.vo.UserFollowVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserFollowMapper extends BaseMapper<UserFollow, UserFollowQuery> {

    /**
     * 我的关注列表（关联用户信息）
     */
    List<UserFollowVO> selectFollowList(@Param("query") UserFollowQuery query);

    Integer selectFollowCount(@Param("query") UserFollowQuery query);

    /**
     * 粉丝列表（关联用户信息）
     */
    List<UserFollowVO> selectFansList(@Param("query") UserFollowQuery query);

    Integer selectFansCount(@Param("query") UserFollowQuery query);
}
