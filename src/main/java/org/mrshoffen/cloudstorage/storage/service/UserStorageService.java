package org.mrshoffen.cloudstorage.storage.service;


import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.mrshoffen.cloudstorage.storage.dto.DownloadStorageObjectDto;
import org.mrshoffen.cloudstorage.storage.dto.StorageObject;
import org.mrshoffen.cloudstorage.storage.dto.StorageObjectDto;
import org.mrshoffen.cloudstorage.storage.dto.request.CopyMoveRequest;
import org.mrshoffen.cloudstorage.storage.mapper.FolderFileMapper;
import org.mrshoffen.cloudstorage.storage.repository.MinioRepository;
import org.mrshoffen.cloudstorage.storage.repository.StorageObjectFactory;
import org.mrshoffen.cloudstorage.storage.repository.minio.MinioFileService;
import org.mrshoffen.cloudstorage.storage.repository.minio.MinioFolderService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserStorageService {

    private final MinioRepository minioRepository;

    private final FolderFileMapper mapper;

    private final MinioFolderService minioFolderService;

    private final MinioFileService minioFileService;

    private final StorageObjectFactory factory;


    @SneakyThrows
    public List<StorageObject> storageObjectsFromPath(Long userId, String folderPath) {
        String userRootFolder = userId.toString() + "/";
        String fullPathToFolder = userRootFolder + folderPath;

        List<StorageObject> objects =factory.getService(fullPathToFolder)
                        .findStorageObjectsWithPrefix(fullPathToFolder);

        return objects;
    }

    public List<StorageObject> rootStorageObjects(Long userId) {
        return storageObjectsFromPath(userId, "");
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

        factory.getService(fullSourcePath)
                .copyStorageObject(fullSourcePath, fullTargetPath);
    }


    @SneakyThrows
    public void deleteUserItems(Long userId, String deletePath) {
        String userRootFolder = userId.toString() + "/";
        String fullDeletePath = userRootFolder + deletePath;

        factory.getService(fullDeletePath)
                .deleteStorageObject(fullDeletePath);

    }

    @SneakyThrows
    public void moveUserItems(Long userId, CopyMoveRequest copyDto) {
        String userRootFolder = userId.toString() + "/";
        String fullSourcePath = userRootFolder + copyDto.sourcePath();
        String fullTargetPath = userRootFolder + copyDto.targetPath();

        factory.getService(fullSourcePath)
                .moveStorageObject(fullSourcePath, fullTargetPath);
    }


    static boolean isFolderPath(String fullTargetPath) {
        return fullTargetPath.endsWith("/");
    }

}
