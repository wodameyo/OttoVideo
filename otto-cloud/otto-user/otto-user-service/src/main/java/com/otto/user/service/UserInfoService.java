package com.otto.user.service;

import com.otto.user.api.dto.UserBaseInfoDTO;
import com.otto.user.entity.po.UserInfo;
import com.otto.user.entity.vo.LoginUserVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserInfoService {

    void register(String email, String nickName, String password, String emailCode);

    LoginUserVO login(String email, String password, String checkCodeKey, String checkCode);

    void logout(String token);

    UserInfo getUserInfo(String userId);

    void updateUserInfo(UserInfo userInfo);

    String uploadAvatar(MultipartFile file);

    List<UserBaseInfoDTO> listBaseInfoByIds(List<String> userIds);
}
