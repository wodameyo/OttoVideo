package com.otto.user.controller;

import com.otto.common.constants.Constants;
import com.otto.common.controller.ABaseController;
import com.otto.common.entity.vo.ResponseVO;
import com.otto.user.annotation.GlobalInterceptor;
import com.otto.user.entity.vo.CheckCodeVO;
import com.otto.user.entity.vo.LoginUserVO;
import com.otto.user.redis.UserRedisComponent;
import com.otto.user.service.EmailCodeService;
import com.otto.user.service.UserInfoService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 账号：图形验证码 / 邮箱验证码 / 注册 / 登录 / 登出
 */
@RestController
@RequestMapping("/account")
public class AccountController extends ABaseController {

    @Resource
    private UserRedisComponent redisComponent;

    @Resource
    private EmailCodeService emailCodeService;

    @Resource
    private UserInfoService userInfoService;

    /**
     * 获取图形验证码
     */
    @GetMapping("/checkCode")
    public ResponseVO<CheckCodeVO> checkCode() {
        String[] code = redisComponent.saveCheckCode();
        CheckCodeVO vo = new CheckCodeVO();
        vo.setCheckCodeKey(code[0]);
        vo.setCheckCode(code[1]);
        return getSuccessResponseVO(vo);
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/sendEmailCode")
    public ResponseVO<Object> sendEmailCode(String email, Integer type,
                                            String checkCodeKey, String checkCode) {
        emailCodeService.sendEmailCode(email, type, checkCodeKey, checkCode);
        return getSuccessResponseVO(null);
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public ResponseVO<Object> register(String email, String nickName, String password, String emailCode) {
        userInfoService.register(email, nickName, password, emailCode);
        return getSuccessResponseVO(null);
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public ResponseVO<LoginUserVO> login(String email, String password,
                                         String checkCodeKey, String checkCode) {
        return getSuccessResponseVO(userInfoService.login(email, password, checkCodeKey, checkCode));
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO<Object> logout(HttpServletRequest request) {
        userInfoService.logout(request.getHeader(Constants.HEADER_TOKEN));
        return getSuccessResponseVO(null);
    }
}
