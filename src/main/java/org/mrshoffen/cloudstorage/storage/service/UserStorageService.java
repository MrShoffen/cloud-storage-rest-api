package org.mrshoffen.cloudstorage.storage.service;


import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.mrshoffen.cloudstorage.storage.dto.StorageObjectResourceDto;
import org.mrshoffen.cloudstorage.storage.dto.StorageObject;
import org.mrshoffen.cloudstorage.storage.dto.StorageObjectDto;
import org.mrshoffen.cloudstorage.storage.dto.request.CopyMoveRequest;
import org.mrshoffen.cloudstorage.storage.repository.MinioOperationResolver;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserStorageService {

    private final MinioOperationResolver factory;


    @SneakyThrows
    public List<StorageObject> storageObjectsFromPath(Long userId, String folderPath) {
        String userRootFolder = userId.toString() + "/";
        String fullPathToFolder = userRootFolder + folderPath;

        List<StorageObject> objects = factory.resolveOperation(fullPathToFolder)
                .findObjectWithPrefix(fullPathToFolder);

        return objects;
    }

    public List<StorageObject> rootStorageObjects(Long userId) {
        return storageObjectsFromPath(userId, "");
    }


    public StorageObjectResourceDto downloadUserItems(Long userId, String objectPath) {
        String userRootFolder = userId.toString() + "/";
        String fullObjectPath = userRootFolder + objectPath;

        StorageObjectDto storageObject;


        return factory.resolveOperation(fullObjectPath)
                .downloadObject(fullObjectPath);
    }


    public void copyUserItems(Long userId, CopyMoveRequest copyDto) {
        String userRootFolder = userId.toString() + "/";
        String fullSourcePath = userRootFolder + copyDto.sourcePath();
        String fullTargetPath = userRootFolder + copyDto.targetPath();

        factory.resolveOperation(fullSourcePath)
                .copyObject(fullSourcePath, fullTargetPath);
    }


    @SneakyThrows
    public void deleteUserItems(Long userId, String deletePath) {
        String userRootFolder = userId.toString() + "/";
        String fullDeletePath = userRootFolder + deletePath;

        factory.resolveOperation(fullDeletePath)
                .deleteObjectByPath(fullDeletePath);

    }

    @SneakyThrows
    public void moveUserItems(Long userId, CopyMoveRequest copyDto) {
        String userRootFolder = userId.toString() + "/";
        String fullSourcePath = userRootFolder + copyDto.sourcePath();
        String fullTargetPath = userRootFolder + copyDto.targetPath();

        factory.resolveOperation(fullSourcePath)
                .moveObject(fullSourcePath, fullTargetPath);
    }


    static boolean isFolderPath(String fullTargetPath) {
        return fullTargetPath.endsWith("/");
    }

}
