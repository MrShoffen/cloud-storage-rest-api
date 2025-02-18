package org.mrshoffen.cloudstorage.storage.minio;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.mrshoffen.cloudstorage.storage.model.dto.response.StorageObjectResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
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


    @SneakyThrows
    public void saveFolderContent(String path, List<StorageObjectResponse> folderContent, long ttlInSeconds) {
        String json = objectMapper.writeValueAsString(folderContent); // сериализуем List в JSON
        redisTemplate.opsForValue().set(path, json, ttlInSeconds, TimeUnit.SECONDS); // сохраняем в Redis с TTL
    }

    @SneakyThrows
    public List<StorageObjectResponse> getFolderContent(String path) {
        String json = redisTemplate.opsForValue().get(path); // получаем JSON из Redis
        if (json != null) {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        }

        return Collections.emptyList();
    }

    public void deleteFolderContent(String path) {
        redisTemplate.delete(path);
    }


}
