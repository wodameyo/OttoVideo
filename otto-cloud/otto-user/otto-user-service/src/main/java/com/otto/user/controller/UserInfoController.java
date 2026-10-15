package com.otto.user.controller;

import com.otto.common.controller.ABaseController;
import com.otto.common.entity.vo.ResponseVO;
import com.otto.user.annotation.GlobalInterceptor;
import com.otto.user.entity.po.UserInfo;
import com.otto.user.service.UserInfoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 用户资料
 */
@RestController
@RequestMapping("/user")
public class UserInfoController extends ABaseController {

    @Resource
    private UserInfoService userInfoService;

    /**
     * 用户主页信息（公开）
     */
    @GetMapping("/getUserInfo")
    public ResponseVO<UserInfo> getUserInfo(@RequestParam("userId") String userId) {
        return getSuccessResponseVO(userInfoService.getUserInfo(userId));
    }

    /**
     * 当前登录用户信息
     */
    @GetMapping("/currentUser")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO<UserInfo> currentUser() {
        return getSuccessResponseVO(userInfoService.getUserInfo(getTokenUserInfo().getUserId()));
    }

    /**
     * 修改资料
     */
    @PostMapping("/updateUserInfo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO<Object> updateUserInfo(UserInfo userInfo) {
        userInfoService.updateUserInfo(userInfo);
        return getSuccessResponseVO(null);
    }

    /**
     * 上传头像，返回 {url}
     */
    @PostMapping("/uploadAvatar")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String url = userInfoService.uploadAvatar(file);
        return getSuccessResponseVO(Map.of("url", url));
    }
}
