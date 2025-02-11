package org.mrshoffen.cloudstorage.storage.service;


import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.mrshoffen.cloudstorage.storage.dto.request.CopyMoveRequest;
import org.mrshoffen.cloudstorage.storage.dto.response.FolderFileResponseDto;
import org.mrshoffen.cloudstorage.storage.exception.ConflictFileNameException;
import org.mrshoffen.cloudstorage.storage.mapper.FolderFileMapper;
import org.mrshoffen.cloudstorage.storage.repository.MinioRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioRepository minioRepository;

    private final FolderFileMapper mapper;


    @SneakyThrows
    public List<FolderFileResponseDto> userFolderItems(Long userId, String folderPath) {
        String userRootFolder = userId.toString() + "/";
        String fullPathToFolder = userRootFolder + folderPath;

        List<Item> objects = minioRepository.getFolderItems(fullPathToFolder);

        List<FolderFileResponseDto> filesAndFolders = objects.stream()
                .map(mapper::toDto)
                .toList();

        return filesAndFolders.stream()
                .toList();
    }

    public List<FolderFileResponseDto> usersRootFolderContent(Long userId) {
        return userFolderItems(userId, "");
    }


    public Resource downloadUserItems(Long userId, String objectPath) {
        String userRootFolder = userId.toString() + "/";
        String fullObjectPath = userRootFolder + objectPath;

        if (isFolderPath(fullObjectPath)) {

        } else {
            minioRepository.downloadFile(fullObjectPath);
        }
        return null;
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
