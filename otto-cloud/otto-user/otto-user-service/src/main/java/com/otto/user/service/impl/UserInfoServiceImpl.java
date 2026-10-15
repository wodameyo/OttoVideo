package com.otto.user.service.impl;

import com.otto.common.constants.Constants;
import com.otto.common.entity.dto.TokenUserInfoDTO;
import com.otto.common.entity.enums.PageSize;
import com.otto.common.entity.query.SimplePage;
import com.otto.common.entity.enums.ResponseCodeEnum;
import com.otto.common.exception.BusinessException;
import com.otto.common.utils.CopyTools;
import com.otto.common.utils.StringTools;
import com.otto.user.api.dto.UserBaseInfoDTO;
import com.otto.user.entity.enums.UserRoleEnum;
import com.otto.user.entity.enums.UserStatusEnum;
import com.otto.user.entity.po.UserInfo;
import com.otto.user.entity.query.UserInfoQuery;
import com.otto.user.entity.vo.LoginUserVO;
import com.otto.user.mappers.UserInfoMapper;
import com.otto.user.redis.UserRedisComponent;
import com.otto.user.service.FileService;
import com.otto.user.service.UserInfoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    /** 注册验证码类型 */
    private static final Integer EMAIL_CODE_TYPE_REGISTER = 0;
    /** 登录图形验证码类型 */
    private static final int MAX_RETRY = 5;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private UserRedisComponent redisComponent;

    @Resource
    private FileService fileService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(String email, String nickName, String password, String emailCode) {
        if (!StringTools.isEmail(email)) {
            throw new BusinessException("邮箱格式不正确");
        }
        if (StringTools.isEmpty(nickName) || nickName.length() > 32) {
            throw new BusinessException("昵称为空或过长");
        }
        if (StringTools.isEmpty(password) || password.length() < 6 || password.length() > 20) {
            throw new BusinessException("密码长度为 6~20 位");
        }
        if (!redisComponent.checkEmailCode(email, EMAIL_CODE_TYPE_REGISTER, emailCode)) {
            throw new BusinessException("邮箱验证码错误或已过期");
        }
        UserInfoQuery emailQuery = new UserInfoQuery();
        emailQuery.setEmail(email);
        if (userInfoMapper.selectCount(emailQuery) > 0) {
            throw new BusinessException("该邮箱已注册");
        }
        UserInfo user = new UserInfo();
        user.setUserId(generateUserId());
        user.setEmail(email);
        user.setNickName(nickName);
        String salt = StringTools.getRandomString(5);
        user.setSalt(salt);
        user.setPassword(StringTools.encodeMd5(password + salt));
        user.setSex(0);
        user.setRole(UserRoleEnum.USER.getRole());
        user.setStatus(UserStatusEnum.ENABLE.getStatus());
        user.setIntegral(0);
        user.setTotalFollows(0);
        user.setTotalFans(0);
        user.setTotalLikes(0);
        user.setCreateTime(new Date());
        userInfoMapper.insert(user);
    }

    @Override
    public LoginUserVO login(String email, String password, String checkCodeKey, String checkCode) {
        if (!redisComponent.checkCheckCode(checkCodeKey, checkCode)) {
            throw new BusinessException("图形验证码错误");
        }
        if (!StringTools.isEmail(email) || StringTools.isEmpty(password)) {
            throw new BusinessException("邮箱或密码错误");
        }
        UserInfoQuery query = new UserInfoQuery();
        query.setEmail(email);
        List<UserInfo> list = userInfoMapper.selectList(query);
        if (list.isEmpty()) {
            throw new BusinessException("邮箱或密码错误");
        }
        UserInfo user = list.get(0);
        if (!StringTools.encodeMd5(password + user.getSalt()).equals(user.getPassword())) {
            throw new BusinessException("邮箱或密码错误");
        }
        if (UserStatusEnum.DISABLE.getStatus().equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用");
        }

        // 更新最后登录时间
        UserInfo update = new UserInfo();
        update.setLastLoginTime(new Date());
        UserInfoQuery idQuery = new UserInfoQuery();
        idQuery.setUserId(user.getUserId());
        userInfoMapper.updateByParam(update, idQuery);
        user.setLastLoginTime(update.getLastLoginTime());

        TokenUserInfoDTO tokenUser = new TokenUserInfoDTO();
        tokenUser.setToken(StringTools.getUUID());
        tokenUser.setUserId(user.getUserId());
        tokenUser.setNickName(user.getNickName());
        tokenUser.setAvatar(user.getAvatar());
        tokenUser.setRole(user.getRole());
        redisComponent.saveToken(tokenUser);

        LoginUserVO vo = new LoginUserVO();
        vo.setToken(tokenUser.getToken());
        vo.setUserInfo(user);
        return vo;
    }

    @Override
    public void logout(String token) {
        if (StringTools.isNotEmpty(token)) {
            redisComponent.removeToken(token);
        }
    }

    @Override
    public UserInfo getUserInfo(String userId) {
        if (StringTools.isEmpty(userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        UserInfoQuery query = new UserInfoQuery();
        query.setUserId(userId);
        List<UserInfo> list = userInfoMapper.selectList(query);
        if (list.isEmpty()) {
            throw new BusinessException("用户不存在");
        }
        return list.get(0);
    }

    @Override
    public void updateUserInfo(UserInfo userInfo) {
        String currentUserId = com.otto.common.context.UserContextHolder.getUserId();
        UserInfo update = new UserInfo();
        update.setNickName(userInfo.getNickName());
        update.setSex(userInfo.getSex());
        update.setPersonalSign(userInfo.getPersonalSign());
        update.setBirthday(userInfo.getBirthday());
        update.setAvatar(userInfo.getAvatar());
        UserInfoQuery query = new UserInfoQuery();
        query.setUserId(currentUserId);
        userInfoMapper.updateByParam(update, query);
    }

    @Override
    public String uploadAvatar(MultipartFile file) {
        return fileService.uploadAvatar(file);
    }

    @Override
    public List<UserBaseInfoDTO> listBaseInfoByIds(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<UserInfo> users = userInfoMapper.selectListByUserIds(userIds);
        return CopyTools.copyList(users, UserBaseInfoDTO.class);
    }

    private String generateUserId() {
        for (int i = 0; i < MAX_RETRY; i++) {
            String userId = StringTools.getRandomNumber(11);
            UserInfoQuery query = new UserInfoQuery();
            query.setUserId(userId);
            if (userInfoMapper.selectCount(query) == 0) {
                return userId;
            }
        }
        throw new BusinessException("生成用户ID失败，请重试");
    }
}
