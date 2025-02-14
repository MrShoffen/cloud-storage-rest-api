package org.mrshoffen.cloudstorage.storage.service;


import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.mrshoffen.cloudstorage.storage.exception.StorageObjectAlreadyExistsException;
import org.mrshoffen.cloudstorage.storage.exception.StorageObjectNotFoundException;
import org.mrshoffen.cloudstorage.storage.model.StorageObject;
import org.mrshoffen.cloudstorage.storage.model.dto.response.StorageObjectResourceDto;
import org.mrshoffen.cloudstorage.storage.model.dto.request.CopyMoveRequest;
import org.mrshoffen.cloudstorage.storage.model.dto.response.StorageOperationResponse;
import org.mrshoffen.cloudstorage.storage.repository.StorageObjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class UserStorageService {

    private final StorageObjectRepository repository;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @SneakyThrows
    public List<StorageObject> listObjectsInFolder(Long userId, String folderPath) {
        String userRootFolder = userId.toString() + "/";
        String fullPathToFolder = userRootFolder + folderPath;

        return repository.findAllObjectsInFolder(fullPathToFolder);
    }

    public List<StorageObject> listObjectsInRootFolder(Long userId) {
        return listObjectsInFolder(userId, "");
    }


    public StorageObjectResourceDto downloadObject(Long userId, String objectPath) {
        String userRootFolder = userId.toString() + "/";
        String fullObjectPath = userRootFolder + objectPath;

        return repository.getObject(fullObjectPath);
    }


    public void copyObject(Long userId, CopyMoveRequest copyDto) {
        String userRootFolder = userId.toString() + "/";
        String fullSourcePath = userRootFolder + copyDto.sourcePath();
        String fullTargetPath = userRootFolder + copyDto.targetPath();

        repository.copyObject(fullSourcePath, fullTargetPath);
    }


    @SneakyThrows
    public void deleteObject(Long userId, String deletePath) {
        String userRootFolder = userId.toString() + "/";
        String fullDeletePath = userRootFolder + deletePath;

        repository.deleteObject(fullDeletePath);
    }

    @SneakyThrows
    public void moveObject(Long userId, CopyMoveRequest copyDto) {
        String userRootFolder = userId.toString() + "/";
        String fullSourcePath = userRootFolder + copyDto.sourcePath();
        String fullTargetPath = userRootFolder + copyDto.targetPath();

        repository.moveObject(fullSourcePath, fullTargetPath);
    }

    @SneakyThrows
    public List<StorageOperationResponse> uploadObjectsToFolder(Long userId, List<MultipartFile> files, String folder) {
        boolean isRootFolder = folder == null || "".equals(folder);

        String userRootFolder = userId.toString() + "/";
        String fullPathToFolder = userRootFolder + (isRootFolder ? "" : folder);

//        if (!isRootFolder && repository.findAllObjectsInFolder(fullPathToFolder).isEmpty()) {
//            throw new StorageObjectNotFoundException("Папка с именем '%s' не существует"
//                    .formatted(folder));
//        }

        Map<String, List<MultipartFile>> innerFolders = new HashMap<>();
        List<MultipartFile> filesWithoutFolder = new ArrayList<>();

        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            int firstSlash = fileName.indexOf("/");

            if (firstSlash == -1) {
                filesWithoutFolder.add(file);
            } else {
                String prefix = fileName.substring(0, firstSlash + 1);
                innerFolders.computeIfAbsent(prefix, k -> new ArrayList<>()).add(file);
            }
        }


        List<StorageOperationResponse> responseList = new ArrayList<>();

        for (MultipartFile file : filesWithoutFolder) {
            StorageOperationResponse response = uploadFile(file, fullPathToFolder, folder);
            responseList.add(response);
        }

        for (String innerFolder : innerFolders.keySet()) {
            StorageOperationResponse response = uploadFolder(
                    innerFolders.get(innerFolder),
                    folder,
                    innerFolder,
                    fullPathToFolder);

            responseList.add(response);
        }

        return responseList;
    }


    @SneakyThrows
    private StorageOperationResponse uploadFile(MultipartFile file, String fullPathToFolder, String folder) {
        try (InputStream stream = file.getInputStream()) {
            String objectPath = fullPathToFolder + file.getOriginalFilename();
            repository.uploadSingleObject(objectPath, stream, file.getSize(), false);

            return StorageOperationResponse.builder()
                    .status(CREATED.value())
                    .title(CREATED.getReasonPhrase())
                    .detail("Файл '%s' успешно загружен".formatted(file.getOriginalFilename()))
                    .path(folder + file.getOriginalFilename())
                    .build();
        } catch (StorageObjectAlreadyExistsException ex) {
            return StorageOperationResponse.builder()
                    .status(CONFLICT.value())
                    .title(CONFLICT.getReasonPhrase())
                    .detail(ex.getMessage())
                    .path(folder + file.getOriginalFilename())
                    .build();
        }

    }

    @SneakyThrows
    private StorageOperationResponse uploadFolder(List<MultipartFile> innerFolder, String baseFolder, String innerFolderName, String fullPathToFolder) {
        List<StorageOperationResponse> responseList = new ArrayList<>();

        List<StorageObject> allObjectsInFolder = repository.findAllObjectsInFolder(fullPathToFolder + innerFolderName);
        if (!allObjectsInFolder.isEmpty()) {
            return StorageOperationResponse.builder()
                    .status(CONFLICT.value())
                    .title(CONFLICT.getReasonPhrase())
                    .detail("Папка '%s' уже существует в целевой директории '%s'".formatted(innerFolderName, baseFolder))
                    .path(baseFolder + innerFolderName)
                    .build();
        }
        for (MultipartFile file : innerFolder) {
            try (InputStream stream = file.getInputStream()) {
                repository.uploadSingleObject(
                        fullPathToFolder + file.getOriginalFilename(),
                        stream,
                        file.getSize(), true);
            }
        }
        return StorageOperationResponse.builder()
                .status(CREATED.value())
                .title(CREATED.getReasonPhrase())
                .detail("Папка '%s' успешно загружена".formatted(innerFolderName))
                .path(baseFolder + innerFolderName)
                .build();

    }
}
