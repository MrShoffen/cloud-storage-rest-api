package org.mrshoffen.cloudstorage.storage.service;


import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.mrshoffen.cloudstorage.storage.dto.FolderFileResponseDto;
import org.mrshoffen.cloudstorage.storage.mapper.FolderFileMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MinioService {

    @Value("${minio.bucket-name}")
    private String bucket;

    private final MinioClient minioClient;

    private final FolderFileMapper mapper;


    @SneakyThrows
    public List<FolderFileResponseDto> usersFolderContent(Long userId, String folderName) {
        String userRootFolder = userId.toString() + "/";
        String fullPathToFolder = userRootFolder + folderName;
        Iterable<Result<Item>> objects = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .prefix(fullPathToFolder)
                        .build()
        );
        List<FolderFileResponseDto> filesAndFolders = new ArrayList<>();
        for (Result<Item> object : objects) {
            Item item = object.get();
            filesAndFolders.add(mapper.toDto(item));
        }

        return filesAndFolders;
    }

    public List<FolderFileResponseDto> usersRootFolderContent(Long userId) {
        return usersFolderContent(userId, "");
    }


}
