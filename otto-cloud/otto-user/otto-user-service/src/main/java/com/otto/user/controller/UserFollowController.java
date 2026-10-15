package com.otto.user.controller;

import com.otto.common.controller.ABaseController;
import com.otto.common.entity.vo.PaginationResultVO;
import com.otto.common.entity.vo.ResponseVO;
import com.otto.user.annotation.GlobalInterceptor;
import com.otto.user.entity.query.UserFollowQuery;
import com.otto.user.entity.vo.UserFollowVO;
import com.otto.user.service.UserFollowService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 关注 / 粉丝
 */
@RestController
@RequestMapping("/follow")
public class UserFollowController extends ABaseController {

    @Resource
    private UserFollowService userFollowService;

    /**
     * 关注 / 取消关注 action: 0关注 1取消
     */
    @PostMapping("/follow")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO<Object> follow(String followUserId, Integer action) {
        userFollowService.follow(getTokenUserInfo().getUserId(), followUserId, action);
        return getSuccessResponseVO(null);
    }

    /**
     * 查询是否已关注
     */
    @GetMapping("/checkFollow")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO<Boolean> checkFollow(String targetUserId) {
        return getSuccessResponseVO(
                userFollowService.checkFollow(getTokenUserInfo().getUserId(), targetUserId));
    }

    /**
     * 关注列表（公开）
     */
    @GetMapping("/loadFollows")
    public ResponseVO<PaginationResultVO<UserFollowVO>> loadFollows(UserFollowQuery query) {
        return getSuccessResponseVO(userFollowService.loadFollows(query));
    }

    /**
     * 粉丝列表（公开）
     */
    @GetMapping("/loadFans")
    public ResponseVO<PaginationResultVO<UserFollowVO>> loadFans(UserFollowQuery query) {
        return getSuccessResponseVO(userFollowService.loadFans(query));
    }
}
