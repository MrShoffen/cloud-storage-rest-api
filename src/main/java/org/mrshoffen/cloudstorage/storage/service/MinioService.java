package org.mrshoffen.cloudstorage.storage.service;


import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.mrshoffen.cloudstorage.storage.dto.request.CopyMoveRequest;
import org.mrshoffen.cloudstorage.storage.dto.response.FolderFileResponseDto;
import org.mrshoffen.cloudstorage.storage.exception.ConflictNameException;
import org.mrshoffen.cloudstorage.storage.mapper.FolderFileMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class MinioService {

    @Value("${minio.bucket-name}")
    private String bucket;

    private final MinioClient minioClient;

    private final FolderFileMapper mapper;


    @SneakyThrows
    public List<FolderFileResponseDto> userFolderItems(Long userId, String folderName) {
        String userRootFolder = userId.toString() + "/";
        String fullPathToFolder = userRootFolder + folderName;

        List<Item> objects = getFolderItems(fullPathToFolder, false);

        List<FolderFileResponseDto> filesAndFolders = objects.stream()
                .map(mapper::toDto)
                .toList();

        return filesAndFolders.stream()
                .sorted((o1, o2) -> Boolean.compare(o2.isFolder(), o1.isFolder()))
                .toList();
    }

    public List<FolderFileResponseDto> usersRootFolderContent(Long userId) {
        return userFolderItems(userId, "");
    }


    @SneakyThrows
    public void copyUserItems(Long userId, CopyMoveRequest copyDto) {
        String userRootFolder = userId.toString() + "/";
        String fullSourcePath = userRootFolder + copyDto.sourcePath();
        String fullTargetPath = userRootFolder + copyDto.targetPath();

        checkForConflict(fullTargetPath);

        if (isFolderPath(fullTargetPath)) {copyDirectory(fullTargetPath, fullSourcePath);
        } else {
            copyFile(fullTargetPath, fullSourcePath);
        }
    }


    @SneakyThrows
    public void deleteUserItems(Long userId, String deletePath) {
        String userRootFolder = userId.toString() + "/";
        String fullDeletePath = userRootFolder + deletePath;

        if (isFolderPath(fullDeletePath)) {
            deleteDirectory(fullDeletePath);
        } else {
            deleteFile(fullDeletePath);
        }
    }

    @SneakyThrows
    public void moveUserItems(Long userId, CopyMoveRequest copyDto) {
        String userRootFolder = userId.toString() + "/";
        String fullSourcePath = userRootFolder + copyDto.sourcePath();
        String fullTargetPath = userRootFolder + copyDto.targetPath();

        checkForConflict(fullTargetPath);


        if (isFolderPath(fullTargetPath)) {
            copyDirectory(fullTargetPath, fullSourcePath);
            deleteDirectory(fullSourcePath);
        } else {
            copyFile(fullTargetPath, fullSourcePath);
            deleteFile(fullSourcePath);
        }
    }


    //todo maybe move to separate service class
    @SneakyThrows
    private void deleteDirectory(String folderDeletePath) {
        List<DeleteObject> listForDelete = getFolderItems(folderDeletePath, true)
                .stream()
                .map(Item::objectName)
                .map(DeleteObject::new)
                .toList();

//todo catch exception - if resource doesnt exist
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                        .bucket(bucket)
                        .objects(listForDelete)
                        .build()
        );

        for (Result<DeleteError> result : results) {
            DeleteError error = result.get();
            String x = "Error in deleting object " + error.objectName() + "; " + error.message();
            System.out.println(
                    x);
        }

    }

    @SneakyThrows
    private void deleteFile(String fileDeletePath) {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(fileDeletePath)
                        .build()
        );

    }

    @SneakyThrows
    private void copyDirectory(String fullTargetPath, String fullSourcePath) {

        getFolderItems(fullSourcePath, true)
                .stream()
                .map(Item::objectName)
                .forEach(sourcePath -> {
                    String targetPath = sourcePath.replaceFirst(fullSourcePath, fullTargetPath);
                    copyFile(targetPath, sourcePath);
                });
    }

    @SneakyThrows
    private void copyFile(String fullTargetPath, String fullSourcePath) {
        minioClient.copyObject(
                CopyObjectArgs.builder()
                        .bucket(bucket)
                        .object(fullTargetPath)
                        .source(
                                CopySource.builder()
                                        .bucket(bucket)
                                        .object(fullSourcePath)
                                        .build()
                        )
                        .build()
        );
    }

    //todo override to 2 functions
    private List<Item> getFolderItems(String fullPathToFolder, boolean recursive) {
        Iterable<Result<Item>> objects = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .recursive(recursive)
                        .prefix(fullPathToFolder)
                        .build()
        );

        return StreamSupport.stream(objects.spliterator(), false)
                .map(result -> {
                            try {
                                return result.get();
                            } catch (Exception e) {
                                //todo throw specified exception
                                throw new RuntimeException("Error occurred while getting items from folder " + fullPathToFolder, e);
                            }
                        }
                )
                .toList();


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
        List<Item> objects = getFolderItems(fullFolderPath, false);
        if (!objects.isEmpty()) {
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
