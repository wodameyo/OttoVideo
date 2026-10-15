package com.otto.user.service.impl;

import com.otto.common.context.UserContextHolder;
import com.otto.common.entity.enums.PageSize;
import com.otto.common.entity.query.SimplePage;
import com.otto.common.entity.vo.PaginationResultVO;
import com.otto.common.exception.BusinessException;
import com.otto.common.utils.StringTools;
import com.otto.user.entity.po.UserFollow;
import com.otto.user.entity.po.UserInfo;
import com.otto.user.entity.query.UserFollowQuery;
import com.otto.user.entity.query.UserInfoQuery;
import com.otto.user.entity.vo.UserFollowVO;
import com.otto.user.mappers.UserFollowMapper;
import com.otto.user.mappers.UserInfoMapper;
import com.otto.user.service.UserFollowService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class UserFollowServiceImpl implements UserFollowService {

    /** 关注 */
    private static final Integer ACTION_FOLLOW = 0;
    /** 取消关注 */
    private static final Integer ACTION_UNFOLLOW = 1;

    @Resource
    private UserFollowMapper userFollowMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void follow(String currentUserId, String followUserId, Integer action) {
        if (StringTools.isEmpty(currentUserId) || currentUserId.equals(followUserId)) {
            throw new BusinessException("关注用户不合法");
        }
        UserInfoQuery userQuery = new UserInfoQuery();
        userQuery.setUserId(followUserId);
        if (userInfoMapper.selectCount(userQuery) == 0) {
            throw new BusinessException("用户不存在");
        }

        UserFollowQuery followQuery = new UserFollowQuery();
        followQuery.setUserId(currentUserId);
        followQuery.setFollowUserId(followUserId);
        boolean exists = userFollowMapper.selectCount(followQuery) > 0;

        if (ACTION_FOLLOW.equals(action)) {
            if (exists) {
                throw new BusinessException("已经关注过了");
            }
            UserFollow follow = new UserFollow();
            follow.setUserId(currentUserId);
            follow.setFollowUserId(followUserId);
            follow.setCreateTime(new Date());
            userFollowMapper.insert(follow);
            // 我的关注数 +1，UP 粉丝数 +1
            userInfoMapper.updateCount(currentUserId, "total_follows", 1);
            userInfoMapper.updateCount(followUserId, "total_fans", 1);
        } else if (ACTION_UNFOLLOW.equals(action)) {
            if (!exists) {
                return;
            }
            userFollowMapper.deleteByParam(followQuery);
            userInfoMapper.updateCount(currentUserId, "total_follows", -1);
            userInfoMapper.updateCount(followUserId, "total_fans", -1);
        } else {
            throw new BusinessException("操作类型不合法");
        }
    }

    @Override
    public Boolean checkFollow(String currentUserId, String targetUserId) {
        if (StringTools.isEmpty(currentUserId) || StringTools.isEmpty(targetUserId)) {
            return false;
        }
        UserFollowQuery query = new UserFollowQuery();
        query.setUserId(currentUserId);
        query.setFollowUserId(targetUserId);
        return userFollowMapper.selectCount(query) > 0;
    }

    @Override
    public PaginationResultVO<UserFollowVO> loadFollows(UserFollowQuery query) {
        return page(query, false);
    }

    @Override
    public PaginationResultVO<UserFollowVO> loadFans(UserFollowQuery query) {
        return page(query, true);
    }

    private PaginationResultVO<UserFollowVO> page(UserFollowQuery query, boolean fans) {
        int pageNo = query.getPageNo() == null ? 1 : query.getPageNo();
        int pageSize = query.getPageSize() == null ? PageSize.SIZE20.getSize() : query.getPageSize();
        Integer count = fans ? userFollowMapper.selectFansCount(query) : userFollowMapper.selectFollowCount(query);
        SimplePage simplePage = new SimplePage(pageNo, count, pageSize);
        query.setSimplePage(simplePage);
        List<UserFollowVO> list = fans
                ? userFollowMapper.selectFansList(query)
                : userFollowMapper.selectFollowList(query);
        // 当前登录用户是否已关注列表中的用户
        String currentUserId = UserContextHolder.getUserId();
        if (currentUserId != null) {
            for (UserFollowVO vo : list) {
                vo.setFollowed(checkFollow(currentUserId, vo.getUserId()));
            }
        }
        return new PaginationResultVO<>(count, pageSize, simplePage.getPageNo(), list);
    }
}
