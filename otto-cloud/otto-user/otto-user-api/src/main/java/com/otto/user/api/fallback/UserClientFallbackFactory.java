package com.otto.user.api.fallback;

import com.otto.common.entity.enums.ResponseCodeEnum;
import com.otto.common.entity.vo.ResponseVO;
import com.otto.user.api.UserFeignClient;
import com.otto.user.api.dto.UserBaseInfoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户服务降级：用户中心不可用时返回空数据，保证主链路可用
 */
@Component
public class UserClientFallbackFactory implements FallbackFactory<UserFeignClient> {

    private static final Logger logger = LoggerFactory.getLogger(UserClientFallbackFactory.class);

    @Override
    public UserFeignClient create(Throwable cause) {
        logger.error("调用 otto-user-service 失败，触发降级", cause);
        return new UserFeignClient() {
            @Override
            public ResponseVO<List<UserBaseInfoDTO>> listByIds(List<String> userIds) {
                ResponseVO<List<UserBaseInfoDTO>> vo = new ResponseVO<>();
                vo.setCode(ResponseCodeEnum.CODE_500.getCode());
                vo.setInfo("用户服务暂时不可用");
                vo.setData(List.of());
                return vo;
            }

            @Override
            public ResponseVO<UserBaseInfoDTO> getById(String userId) {
                ResponseVO<UserBaseInfoDTO> vo = new ResponseVO<>();
                vo.setCode(ResponseCodeEnum.CODE_500.getCode());
                vo.setInfo("用户服务暂时不可用");
                return vo;
            }
        };
    }
}
