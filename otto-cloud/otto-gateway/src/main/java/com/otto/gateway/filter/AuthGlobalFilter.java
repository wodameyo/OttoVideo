package com.otto.gateway.filter;

import com.alibaba.fastjson2.JSON;
import com.otto.common.constants.Constants;
import com.otto.common.entity.dto.TokenUserInfoDTO;
import com.otto.common.entity.enums.ResponseCodeEnum;
import com.otto.common.entity.vo.ResponseVO;
import com.otto.gateway.config.AuthProperties;
import jakarta.annotation.Resource;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 网关全局鉴权：
 * 1. 白名单 / 预检请求直接放行
 * 2. 校验请求头 token（Redis 登录态）
 * 3. 将用户信息透传给下游（X-User-Id 等请求头）
 * 4. /api/admin/** 要求管理员角色
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Resource
    private ReactiveStringRedisTemplate redisTemplate;

    @Resource
    private AuthProperties authProperties;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 跨域预检放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod().name())) {
            return chain.filter(exchange);
        }
        // 白名单放行
        if (isMatch(path, authProperties.getWhitePaths())) {
            return chain.filter(exchange);
        }

        String token = request.getHeaders().getFirst(Constants.HEADER_TOKEN);
        if (token == null || token.isBlank()) {
            return writeError(exchange, HttpStatus.UNAUTHORIZED, ResponseCodeEnum.CODE_901);
        }

        return redisTemplate.opsForValue().get(Constants.REDIS_KEY_TOKEN_WEB + token)
                .flatMap(json -> {
                    TokenUserInfoDTO user = JSON.parseObject(json, TokenUserInfoDTO.class);
                    if (user == null || user.getUserId() == null) {
                        return writeError(exchange, HttpStatus.UNAUTHORIZED, ResponseCodeEnum.CODE_901);
                    }
                    // 管理员路径角色校验
                    if (isMatch(path, authProperties.getAdminPaths())
                            && !Integer.valueOf(1).equals(user.getRole())) {
                        return writeError(exchange, HttpStatus.FORBIDDEN, ResponseCodeEnum.CODE_403);
                    }
                    ServerHttpRequest mutated = request.mutate()
                            .header(Constants.HEADER_USER_ID, user.getUserId())
                            .header(Constants.HEADER_NICK_NAME, encode(user.getNickName()))
                            .header(Constants.HEADER_AVATAR, encode(user.getAvatar()))
                            .header(Constants.HEADER_ROLE, String.valueOf(user.getRole() == null ? 0 : user.getRole()))
                            .build();
                    return chain.filter(exchange.mutate().request(mutated).build());
                })
                .switchIfEmpty(writeError(exchange, HttpStatus.UNAUTHORIZED, ResponseCodeEnum.CODE_901));
    }

    private boolean isMatch(String path, java.util.List<String> patterns) {
        if (patterns == null) {
            return false;
        }
        for (String pattern : patterns) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private String encode(String value) {
        return value == null ? "" : URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private Mono<Void> writeError(ServerWebExchange exchange, HttpStatus httpStatus, ResponseCodeEnum codeEnum) {
        ResponseVO<Object> vo = new ResponseVO<>();
        vo.setStatus(Constants.STATUS_ERROR);
        vo.setCode(codeEnum.getCode());
        vo.setInfo(codeEnum.getMsg());
        byte[] bytes = JSON.toJSONString(vo).getBytes(StandardCharsets.UTF_8);
        exchange.getResponse().setStatusCode(httpStatus);
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
