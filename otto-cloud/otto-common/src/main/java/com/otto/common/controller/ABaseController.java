package com.otto.common.controller;

import com.otto.common.constants.Constants;
import com.otto.common.context.UserContextHolder;
import com.otto.common.entity.dto.TokenUserInfoDTO;
import com.otto.common.entity.enums.ResponseCodeEnum;
import com.otto.common.entity.vo.ResponseVO;
import com.otto.common.exception.BusinessException;
import com.otto.common.redis.RedisUtils;
import com.otto.common.utils.StringTools;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Controller 基类（统一响应 + 登录用户解析）
 */
public abstract class ABaseController {

    @Resource
    protected RedisUtils redisUtils;

    protected <T> ResponseVO<T> getSuccessResponseVO(T t) {
        ResponseVO<T> vo = new ResponseVO<>();
        vo.setStatus(Constants.STATUS_SUCCESS);
        vo.setCode(ResponseCodeEnum.CODE_200.getCode());
        vo.setInfo(ResponseCodeEnum.CODE_200.getMsg());
        vo.setData(t);
        return vo;
    }

    protected <T> ResponseVO<T> getErrorResponseVO(Integer code, String msg, T t) {
        ResponseVO<T> vo = new ResponseVO<>();
        vo.setStatus(Constants.STATUS_ERROR);
        vo.setCode(code);
        vo.setInfo(msg);
        vo.setData(t);
        return vo;
    }

    protected <T> ResponseVO<T> getBusinessErrorResponseVO(BusinessException e, T t) {
        ResponseVO<T> vo = new ResponseVO<>();
        vo.setStatus(Constants.STATUS_ERROR);
        vo.setCode(e.getCode() == null ? ResponseCodeEnum.CODE_600.getCode() : e.getCode());
        vo.setInfo(e.getMessage());
        vo.setData(t);
        return vo;
    }

    protected <T> ResponseVO<T> getServerErrorResponseVO(T t) {
        ResponseVO<T> vo = new ResponseVO<>();
        vo.setStatus(Constants.STATUS_ERROR);
        vo.setCode(ResponseCodeEnum.CODE_500.getCode());
        vo.setInfo(ResponseCodeEnum.CODE_500.getMsg());
        vo.setData(t);
        return vo;
    }

    /**
     * 获取当前登录用户：优先取网关注入的上下文，兼容直连服务时用 token 头查 Redis
     */
    protected TokenUserInfoDTO getTokenUserInfo() {
        TokenUserInfoDTO user = UserContextHolder.get();
        if (user != null) {
            return user;
        }
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }
            HttpServletRequest request = attributes.getRequest();
            String token = request.getHeader(Constants.HEADER_TOKEN);
            if (StringTools.isEmpty(token)) {
                return null;
            }
            return redisUtils.get(Constants.REDIS_KEY_TOKEN_WEB + token, TokenUserInfoDTO.class);
        } catch (Exception e) {
            return null;
        }
    }
}
