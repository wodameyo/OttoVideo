package com.otto.user.mappers;

import com.otto.common.mappers.BaseMapper;
import com.otto.user.entity.po.UserInfo;
import com.otto.user.entity.query.UserInfoQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserInfoMapper extends BaseMapper<UserInfo, UserInfoQuery> {

    /**
     * 按用户 ID 集合批量查询
     */
    List<UserInfo> selectListByUserIds(@Param("userIds") List<String> userIds);

    /**
     * 冗余计数增减（total_follows / total_fans / total_likes）
     */
    Integer updateCount(@Param("userId") String userId, @Param("field") String field, @Param("delta") Integer delta);
}
