package com.otto.user.api;

import com.otto.common.entity.vo.ResponseVO;
import com.otto.user.api.dto.UserBaseInfoDTO;
import com.otto.user.api.fallback.UserClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 用户中心内部 Feign 接口（不经过网关，/inner/** 仅服务间调用）
 */
@FeignClient(name = "otto-user-service", contextId = "userClient", path = "/inner/user",
        fallbackFactory = UserClientFallbackFactory.class)
public interface UserFeignClient {

    /**
     * 根据用户 ID 批量查询基础信息（避免 N+1）
     */
    @GetMapping("/list")
    ResponseVO<List<UserBaseInfoDTO>> listByIds(@RequestParam("userIds") List<String> userIds);

    /**
     * 查询单个用户基础信息
     */
    @GetMapping("/{userId}")
    ResponseVO<UserBaseInfoDTO> getById(@PathVariable("userId") String userId);
}
