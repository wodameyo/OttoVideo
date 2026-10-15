package com.otto.user.service;

import com.otto.common.entity.vo.PaginationResultVO;
import com.otto.user.entity.query.UserFollowQuery;
import com.otto.user.entity.vo.UserFollowVO;

public interface UserFollowService {

    /**
     * 关注/取关
     *
     * @param currentUserId 当前登录用户
     * @param followUserId  被操作用户
     * @param action        0 关注 1 取消
     */
    void follow(String currentUserId, String followUserId, Integer action);

    Boolean checkFollow(String currentUserId, String targetUserId);

    PaginationResultVO<UserFollowVO> loadFollows(UserFollowQuery query);

    PaginationResultVO<UserFollowVO> loadFans(UserFollowQuery query);
}
