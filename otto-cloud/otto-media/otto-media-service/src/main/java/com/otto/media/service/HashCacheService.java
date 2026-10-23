package com.otto.media.service;

import com.otto.common.constants.Constants;
import com.otto.common.redis.RedisUtils;
import com.otto.media.entity.dto.UploadHashCacheDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 秒传缓存：文件 hash -> 已完成转码产物（TTL 7 天）
 */
@Component
public class HashCacheService {

    private static final long EXPIRE_SECONDS = 7L * 24 * 60 * 60;

    @Resource
    private RedisUtils redisUtils;

    public UploadHashCacheDTO get(String hash) {
        return redisUtils.get(Constants.REDIS_KEY_UPLOAD_HASH + hash, UploadHashCacheDTO.class);
    }

    public void put(String hash, UploadHashCacheDTO cache) {
        if (hash == null || hash.isBlank() || cache == null) {
            return;
        }
        redisUtils.setex(Constants.REDIS_KEY_UPLOAD_HASH + hash, cache, EXPIRE_SECONDS);
    }
}
