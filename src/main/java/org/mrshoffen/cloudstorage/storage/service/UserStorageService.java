package org.mrshoffen.cloudstorage.storage.service;


import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.mrshoffen.cloudstorage.storage.model.StorageObject;
import org.mrshoffen.cloudstorage.storage.model.dto.response.StorageObjectResourceDto;
import org.mrshoffen.cloudstorage.storage.model.dto.request.CopyMoveRequest;
import org.mrshoffen.cloudstorage.storage.repository.StorageObjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserStorageService {

   private final StorageObjectRepository repository;

    @SneakyThrows
    public List<StorageObject> getObjectsInFolder(Long userId, String folderPath) {
        String userRootFolder = userId.toString() + "/";
        String fullPathToFolder = userRootFolder + folderPath;

        return repository.find(fullPathToFolder);
    }

    public List<StorageObject> getObjectsInRootFolder(Long userId) {
        return getObjectsInFolder(userId, "");
    }


    public StorageObjectResourceDto downloadObject(Long userId, String objectPath) {
        String userRootFolder = userId.toString() + "/";
        String fullObjectPath = userRootFolder + objectPath;

        return repository.download(fullObjectPath);
    }


    public void copyObject(Long userId, CopyMoveRequest copyDto) {
        String userRootFolder = userId.toString() + "/";
        String fullSourcePath = userRootFolder + copyDto.sourcePath();
        String fullTargetPath = userRootFolder + copyDto.targetPath();

        repository.copy(fullSourcePath, fullTargetPath);
    }


    @SneakyThrows
    public void deleteObject(Long userId, String deletePath) {
        String userRootFolder = userId.toString() + "/";
        String fullDeletePath = userRootFolder + deletePath;

        repository.delete(fullDeletePath);
    }

    @SneakyThrows
    public void moveObject(Long userId, CopyMoveRequest copyDto) {
        String userRootFolder = userId.toString() + "/";
        String fullSourcePath = userRootFolder + copyDto.sourcePath();
        String fullTargetPath = userRootFolder + copyDto.targetPath();

        repository.move(fullSourcePath, fullTargetPath);
    }

}
