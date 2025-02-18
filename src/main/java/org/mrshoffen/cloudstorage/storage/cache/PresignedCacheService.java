package org.mrshoffen.cloudstorage.storage.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PresignedCacheService {

    private final RedisTemplate<String, String> redisTemplate;

    public void savePresignedUrl(String filePath, String presignedUrl, long ttlInSeconds) {
        redisTemplate.opsForValue().set(filePath, presignedUrl, ttlInSeconds, TimeUnit.SECONDS);
    }

    public String getPresignedUrl(String filePath) {
        return redisTemplate.opsForValue().get(filePath);
    }

}
