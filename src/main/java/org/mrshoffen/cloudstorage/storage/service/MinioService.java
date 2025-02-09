package org.mrshoffen.cloudstorage.storage.service;


import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.mrshoffen.cloudstorage.storage.dto.request.CopyRequestDto;
import org.mrshoffen.cloudstorage.storage.dto.response.FolderFileResponseDto;
import org.mrshoffen.cloudstorage.storage.exception.ConflictNameException;
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
        Iterable<Result<Item>> objects = getFolderContent(fullPathToFolder);
        List<FolderFileResponseDto> filesAndFolders = new ArrayList<>();
        for (Result<Item> object : objects) {
            Item item = object.get();
            filesAndFolders.add(mapper.toDto(item));
        }

        return filesAndFolders.stream()
                .sorted((o1, o2) -> Boolean.compare(o2.isFolder(), o1.isFolder()))
                .toList();
    }

    public List<FolderFileResponseDto> usersRootFolderContent(Long userId) {
        return usersFolderContent(userId, "");
    }


    public void copyUserFiles(Long userId, CopyRequestDto copyDto) {
        String userRootFolder = userId.toString() + "/";
        String fullTargetPath = userRootFolder + copyDto.targetPath();

        checkForConflict(fullTargetPath);

    }


    //todo maybe move to separate service class
    private Iterable<Result<Item>> getFolderContent(String fullPathToFolder) {
        Iterable<Result<Item>> objects = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)

                        .prefix(fullPathToFolder)
                        .build()
        );
        return objects;
    }

    private void checkForConflict(String fullTargetPath) {
        if (isFolderPath(fullTargetPath)) {
            checkFolderConflict(fullTargetPath);
        } else {
            checkFileConflict(fullTargetPath);
        }
    }

    @SneakyThrows
    private void checkFileConflict(String fullFilePath) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(fullFilePath)
                            .build());
            throw new ConflictNameException("'%s'already exists in destination folder"
                    .formatted(extractSimpleName(fullFilePath)));
        } catch (MinioException e) {
        }
    }

    private void checkFolderConflict(String fullFolderPath) {
        Iterable<Result<Item>> objects = getFolderContent(fullFolderPath);
        if (objects.iterator().hasNext()) {
            throw new ConflictNameException("'%s'already exists in destination folder"
                    .formatted(extractSimpleName(fullFolderPath)));
        }
    }

    private static boolean isFolderPath(String fullTargetPath) {
        return fullTargetPath.endsWith("/");
    }

    private static String extractSimpleName(String fullPath) {
        int lastSlashIndex = fullPath.lastIndexOf('/', fullPath.length() - 2);
        return fullPath.substring(lastSlashIndex + 1);
    }

}
