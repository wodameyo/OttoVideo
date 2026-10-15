package com.otto.user.controller.inner;

import com.otto.common.controller.ABaseController;
import com.otto.common.entity.vo.ResponseVO;
import com.otto.user.api.dto.UserBaseInfoDTO;
import com.otto.user.service.UserInfoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户中心内部接口（仅服务间 Feign 调用，不经过网关暴露）
 */
@RestController
@RequestMapping("/inner/user")
public class InnerUserController extends ABaseController {

    @Resource
    private UserInfoService userInfoService;

    @GetMapping("/list")
    public ResponseVO<List<UserBaseInfoDTO>> listByIds(@RequestParam("userIds") List<String> userIds) {
        return getSuccessResponseVO(userInfoService.listBaseInfoByIds(userIds));
    }

    @GetMapping("/{userId}")
    public ResponseVO<UserBaseInfoDTO> getById(@PathVariable("userId") String userId) {
        List<UserBaseInfoDTO> list = userInfoService.listBaseInfoByIds(List.of(userId));
        return getSuccessResponseVO(list.isEmpty() ? null : list.get(0));
    }
}
