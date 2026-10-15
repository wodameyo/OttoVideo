package com.otto.user.redis;

import com.otto.common.constants.Constants;
import com.otto.common.entity.dto.TokenUserInfoDTO;
import com.otto.common.redis.RedisUtils;
import com.wf.captcha.SpecCaptcha;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 用户中心 Redis 操作：登录态、图形验证码、邮箱验证码
 */
@Component
public class UserRedisComponent {

    /** 图形验证码 10 分钟 */
    private static final long CHECK_CODE_EXPIRE = 10 * 60L;
    /** 邮箱验证码 15 分钟 */
    private static final long EMAIL_CODE_EXPIRE = 15 * 60L;
    /** 图形验证码宽高 */
    private static final int CODE_WIDTH = 130;
    private static final int CODE_HEIGHT = 38;
    private static final int CODE_LEN = 4;

    @Resource
    private RedisUtils redisUtils;

    /**
     * 生成图形验证码，返回 [checkCodeKey, base64]
     */
    public String[] saveCheckCode() {
        SpecCaptcha captcha = new SpecCaptcha(CODE_WIDTH, CODE_HEIGHT, CODE_LEN);
        String key = UUID.randomUUID().toString().replace("-", "");
        redisUtils.setString(Constants.REDIS_KEY_CHECK_CODE + key, captcha.text().toLowerCase(), CHECK_CODE_EXPIRE);
        return new String[]{key, captcha.toBase64()};
    }

    public boolean checkCheckCode(String key, String code) {
        if (key == null || code == null) {
            return false;
        }
        String cached = redisUtils.getString(Constants.REDIS_KEY_CHECK_CODE + key);
        if (cached != null && cached.equalsIgnoreCase(code)) {
            redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE + key);
            return true;
        }
        return false;
    }

    public void saveEmailCode(String email, Integer type, String code) {
        redisUtils.setString(emailKey(email, type), code, EMAIL_CODE_EXPIRE);
    }

    public boolean checkEmailCode(String email, Integer type, String code) {
        String key = emailKey(email, type);
        String cached = redisUtils.getString(key);
        if (cached != null && cached.equals(code)) {
            redisUtils.delete(key);
            return true;
        }
        return false;
    }

    public void saveToken(TokenUserInfoDTO user) {
        redisUtils.setex(Constants.REDIS_KEY_TOKEN_WEB + user.getToken(), user,
                Constants.REDIS_KEY_EXPIRES_ONE_DAY);
    }

    public void removeToken(String token) {
        redisUtils.delete(Constants.REDIS_KEY_TOKEN_WEB + token);
    }

    private String emailKey(String email, Integer type) {
        return Constants.REDIS_KEY_EMAIL_CODE + email + ":" + type;
    }
}
