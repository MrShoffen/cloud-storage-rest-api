package org.mrshoffen.cloudstorage.storage.service;


import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.mrshoffen.cloudstorage.storage.dto.DownloadStorageObjectDto;
import org.mrshoffen.cloudstorage.storage.dto.StorageObject;
import org.mrshoffen.cloudstorage.storage.dto.StorageObjectDto;
import org.mrshoffen.cloudstorage.storage.dto.request.CopyMoveRequest;
import org.mrshoffen.cloudstorage.storage.mapper.FolderFileMapper;
import org.mrshoffen.cloudstorage.storage.repository.MinioRepository;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioRepository minioRepository;

    private final FolderFileMapper mapper;


    @SneakyThrows
    public List<StorageObject> userFolderItems(Long userId, String folderPath) {
        String userRootFolder = userId.toString() + "/";
        String fullPathToFolder = userRootFolder + folderPath;

        List<StorageObject> objects = minioRepository.getFolderItems(fullPathToFolder);

        return objects;
    }

    public List<StorageObject> usersRootFolderContent(Long userId) {
        return userFolderItems(userId, "");
    }


    public DownloadStorageObjectDto downloadUserItems(Long userId, String objectPath) {
        String userRootFolder = userId.toString() + "/";
        String fullObjectPath = userRootFolder + objectPath;

        StorageObjectDto storageObject;

        //todo create dto
        if (isFolderPath(fullObjectPath)) {
            storageObject = minioRepository.downloadFolder(fullObjectPath);
        } else {
            storageObject = minioRepository.downloadFile(fullObjectPath);
        }

        return DownloadStorageObjectDto.builder()
                .nameForSave(storageObject.getName())
                .size(storageObject.getSize())
                .downloadResource(new InputStreamResource(storageObject.getInputStream()))
                .build();
    }


    public void copyUserItems(Long userId, CopyMoveRequest copyDto) {
        String userRootFolder = userId.toString() + "/";
        String fullSourcePath = userRootFolder + copyDto.sourcePath();
        String fullTargetPath = userRootFolder + copyDto.targetPath();


        if (isFolderPath(fullTargetPath)) {
            minioRepository.copyDirectory(fullTargetPath, fullSourcePath);
        } else {
            minioRepository.copyFile(fullTargetPath, fullSourcePath);
        }


    }


    @SneakyThrows
    public void deleteUserItems(Long userId, String deletePath) {
        String userRootFolder = userId.toString() + "/";
        String fullDeletePath = userRootFolder + deletePath;

        if (isFolderPath(fullDeletePath)) {
            minioRepository.deleteDirectory(fullDeletePath);
        } else {
            minioRepository.deleteFile(fullDeletePath);
        }
    }

    @SneakyThrows
    public void moveUserItems(Long userId, CopyMoveRequest copyDto) {
        String userRootFolder = userId.toString() + "/";
        String fullSourcePath = userRootFolder + copyDto.sourcePath();
        String fullTargetPath = userRootFolder + copyDto.targetPath();


        if (isFolderPath(fullTargetPath)) {
            minioRepository.copyDirectory(fullTargetPath, fullSourcePath);
            minioRepository.deleteDirectory(fullSourcePath);
        } else {
            minioRepository.copyFile(fullTargetPath, fullSourcePath);
            minioRepository.deleteFile(fullSourcePath);
        }
    }


    static boolean isFolderPath(String fullTargetPath) {
        return fullTargetPath.endsWith("/");
    }

}
