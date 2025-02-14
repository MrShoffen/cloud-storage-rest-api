package org.mrshoffen.cloudstorage.storage.service;


import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.mrshoffen.cloudstorage.storage.exception.StorageObjectAlreadyExistsException;
import org.mrshoffen.cloudstorage.storage.exception.StorageObjectNotFoundException;
import org.mrshoffen.cloudstorage.storage.model.StorageObject;
import org.mrshoffen.cloudstorage.storage.model.dto.response.StorageObjectResourceDto;
import org.mrshoffen.cloudstorage.storage.model.dto.request.CopyMoveRequest;
import org.mrshoffen.cloudstorage.storage.repository.StorageObjectRepository;
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
    public void uploadObjectsToFolder(Long userId, List<MultipartFile> files, String folder) {
        String userRootFolder = userId.toString() + "/";
        String fullPathToFolder = userRootFolder + (folder == null ? "" : folder);

//todo calculate size
        if (repository.findAllObjectsInFolder(fullPathToFolder).isEmpty()) {
            throw new StorageObjectNotFoundException("Папка с именем '%s' не существует"
                    .formatted(folder));
        }


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

        for (MultipartFile file : filesWithoutFolder) {
            try (InputStream stream = file.getInputStream()) {
                repository.uploadSingleObject(
                        fullPathToFolder + file.getOriginalFilename(),
                        stream,
                        file.getSize());
            } catch (StorageObjectAlreadyExistsException ex) {

            }
        }

        for (String innerFolder : innerFolders.keySet()) {
            List<StorageObject> allObjectsInFolder = repository.findAllObjectsInFolder(fullPathToFolder + innerFolder);
            if (allObjectsInFolder.size() > 0) {
                //todo add to response
                continue;
            }

            this.uploadFolder(innerFolders.get(innerFolder), fullPathToFolder);
        }
    }


    @SneakyThrows
    public void uploadObjects(Long userId, List<MultipartFile> files) {
        String userRootFolder = userId.toString() + "/";


        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            int firstSlash = fileName.indexOf("/");

            repository.uploadSingleObject(userRootFolder + fileName, file.getInputStream(), file.getSize());
        }


    }

    @SneakyThrows
    private void uploadFolder(List<MultipartFile> innerFolder, String fullPathToFolder) {
        for (MultipartFile file : innerFolder) {
            try (InputStream stream = file.getInputStream()) {
                repository.uploadSingleObject(
                        fullPathToFolder + file.getOriginalFilename(),
                        stream,
                        file.getSize());
            }
        }
    }
}
