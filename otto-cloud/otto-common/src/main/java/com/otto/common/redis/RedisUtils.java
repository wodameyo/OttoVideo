package com.otto.common.redis;

import com.alibaba.fastjson2.TypeReference;
import com.otto.common.utils.JsonUtils;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具（String 存储 + fastjson2 序列化，网关/服务共用同一格式）
 */
@Component
public class RedisUtils {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public boolean set(String key, Object obj) {
        stringRedisTemplate.opsForValue().set(key, JsonUtils.toJsonString(obj));
        return true;
    }

    public boolean setex(String key, Object obj, long seconds) {
        stringRedisTemplate.opsForValue().set(key, JsonUtils.toJsonString(obj), seconds, TimeUnit.SECONDS);
        return true;
    }

    public boolean setString(String key, String value, long seconds) {
        stringRedisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
        return true;
    }

    public String getString(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public <T> T get(String key, Class<T> clazz) {
        return JsonUtils.toObject(stringRedisTemplate.opsForValue().get(key), clazz);
    }

    public <T> List<T> getList(String key, Class<T> clazz) {
        String json = stringRedisTemplate.opsForValue().get(key);
        return json == null ? null : JsonUtils.toList(json, clazz);
    }

    public <T> T get(String key, TypeReference<T> typeReference) {
        String json = stringRedisTemplate.opsForValue().get(key);
        return json == null ? null : JsonUtils.toObject(json, typeReference);
    }

    public Long incr(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }

    public Long incrBy(String key, long delta) {
        return stringRedisTemplate.opsForValue().increment(key, delta);
    }

    public boolean expire(String key, long seconds) {
        return Boolean.TRUE.equals(stringRedisTemplate.expire(key, seconds, TimeUnit.SECONDS));
    }

    public Long getExpire(String key) {
        return stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    public boolean delete(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.delete(key));
    }
}
