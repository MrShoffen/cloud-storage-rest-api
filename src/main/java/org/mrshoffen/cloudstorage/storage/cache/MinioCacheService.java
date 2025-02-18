package org.mrshoffen.cloudstorage.storage.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.storage.model.dto.response.StorageObjectResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MinioCacheService {

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    public void savePresignedUrl(String filePath, String presignedUrl, long ttlInSeconds) {
        redisTemplate.opsForValue().set(filePath, presignedUrl, ttlInSeconds, TimeUnit.SECONDS);
    }

    public String getPresignedUrl(String filePath) {
        return redisTemplate.opsForValue().get(filePath);
    }


    public void saveFolderContent(String path, List<StorageObjectResponse> folderContent, long ttlInSeconds) {
        try {
            String json = objectMapper.writeValueAsString(folderContent); // сериализуем List в JSON
            redisTemplate.opsForValue().set(path, json, ttlInSeconds, TimeUnit.SECONDS); // сохраняем в Redis с TTL
        } catch (Exception e) {
            throw new RuntimeException("Ошибка сериализации объектов", e);
        }
    }

    public List<StorageObjectResponse> getFolderContent(String path) {
        try {
            String json = redisTemplate.opsForValue().get(path); // получаем JSON из Redis
            if (json != null) {
                return objectMapper.readValue(json, new TypeReference<>() {
                });
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка десериализации объектов", e);
        }

        return null;
    }

    public void deleteFolderContent(String path) {
        redisTemplate.delete(path);
    }


}
