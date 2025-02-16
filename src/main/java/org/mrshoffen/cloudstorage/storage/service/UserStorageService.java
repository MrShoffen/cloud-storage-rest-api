package org.mrshoffen.cloudstorage.storage.service;


import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.mrshoffen.cloudstorage.storage.exception.StorageObjectAlreadyExistsException;
import org.mrshoffen.cloudstorage.storage.exception.StorageObjectNotFoundException;
import org.mrshoffen.cloudstorage.storage.exception.StorageQuotaExceededException;
import org.mrshoffen.cloudstorage.storage.model.dto.response.StorageObjectResponse;
import org.mrshoffen.cloudstorage.storage.model.dto.response.StorageObjectResourceDto;
import org.mrshoffen.cloudstorage.storage.model.dto.response.StorageOperationResponse;
import org.mrshoffen.cloudstorage.storage.repository.StorageObjectRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;

@Service
@RequiredArgsConstructor
public class UserStorageService {

    private final StorageObjectRepository repository;

    private final PresignedCacheService cacheService;

    @Value("${minio.presigned-timeout}")
    private int presignedLinkTimeout;

    @Value("${minio.empty-folder-tag}")
    private String emptyFolderTag;

    @Value("${minio.memory-per-user}")
    private long userStorageCapacity;

    public String getPreviewLink(Long userId, String objectPath) {
        String fullPath = getFullPath(userId, objectPath);

        String cachedLink = cacheService.getPresignedUrl(fullPath);
        if (cachedLink != null) {
            return cachedLink;
        }

        String presLink = repository.objectDownloadLink(fullPath, presignedLinkTimeout);

        cacheService.savePresignedUrl(fullPath, presLink, presignedLinkTimeout);

        return presLink;
    }

    public StorageObjectResponse getObjectStats(Long userId, String objectPath) {
        String fullPathToObject = getFullPath(userId, objectPath);
        return repository.objectStats(fullPathToObject)
                .orElseThrow(() -> new StorageObjectNotFoundException("'%s' не существует в исходной папке"
                        .formatted(objectPath)));
    }

    @SneakyThrows
    public List<StorageObjectResponse> listObjectsInFolder(Long userId, String folderPath) {
        String fullPathToFolder = getFullPath(userId, folderPath);
        return repository.allObjectsInFolder(fullPathToFolder);
    }

    public StorageObjectResourceDto downloadObject(Long userId, String objectPath) {
        String fullObjectPath = getFullPath(userId, objectPath);
        return repository.getAsResource(fullObjectPath);
    }

    public void copyObject(Long userId, String from, String to) {
        if (getUsedMemory(userId) > userStorageCapacity * 1024 * 1024) {
            throw new StorageQuotaExceededException("Исчерпан лимит хранилища %d MB".formatted(userStorageCapacity));
        }
        String fullSourcePath = getFullPath(userId, from);
        String fullTargetPath = getFullPath(userId, to);
        repository.copy(fullSourcePath, fullTargetPath);
    }

    @SneakyThrows
    public void deleteObject(Long userId, String deletePath) {
        String fullDeletePath = getFullPath(userId, deletePath);
        repository.delete(fullDeletePath);
    }

    @SneakyThrows
    public void moveObject(Long userId, String from, String to) {
        String fullSourcePath = getFullPath(userId, from);
        String fullTargetPath = getFullPath(userId, to);
        repository.move(fullSourcePath, fullTargetPath);
    }

    public void createFolder(Long userId, String folderPath) {
        String emptyFolderTagPath = getFullPath(userId, folderPath) + emptyFolderTag;
        try {
            repository.safeUpload(emptyFolderTagPath, new ByteArrayInputStream(new byte[0]), 0);
        } catch (StorageObjectAlreadyExistsException e) {
            throw new StorageObjectAlreadyExistsException("Папка '%s' уже существует в целевой директории".formatted(folderPath));
        }
    }

    @SneakyThrows
    public List<StorageOperationResponse> uploadObjectsToFolder(Long userId, List<MultipartFile> files, String targetFolder) {
        String fullPathToTargetFolder = getFullPath(userId, targetFolder);


        long sizeForUpload = files.stream().map(MultipartFile::getSize).reduce(0L, Long::sum);
        if (getUsedMemory(userId) + sizeForUpload > userStorageCapacity * 1024 * 1024) {
            throw new StorageQuotaExceededException("Исчерпан лимит хранилища %d MB".formatted(userStorageCapacity));
        }

        //парсинг файлов и папок для загрузки
        Map<String, List<MultipartFile>> innerFolders = new HashMap<>();
        List<MultipartFile> filesWithoutFolder = new ArrayList<>();
        Set<String> allSubFolders = new HashSet<>();

        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            int firstSlash = fileName.indexOf("/");

            if (firstSlash == -1) {
                filesWithoutFolder.add(file);
            } else {
                String prefix = fileName.substring(0, firstSlash + 1);
                innerFolders.computeIfAbsent(prefix, k -> new ArrayList<>()).add(file);
                allSubFolders.addAll(getAllSubPaths(fileName));
            }
        }

        List<StorageOperationResponse> responseList = new ArrayList<>();

        for (MultipartFile file : filesWithoutFolder) {
            StorageOperationResponse response = uploadFile(file, fullPathToTargetFolder, targetFolder);
            responseList.add(response);
        }

        for (String innerFolder : innerFolders.keySet()) {
            StorageOperationResponse response = uploadFolder(
                    innerFolders.get(innerFolder),
                    targetFolder,
                    innerFolder,
                    fullPathToTargetFolder);

            responseList.add(response);
        }

        //создание пустых тегов у подпапок
        for (String subFolder : allSubFolders) {
            String subfolderEmptyTag = fullPathToTargetFolder + subFolder + emptyFolderTag;
            repository.forceUpload(subfolderEmptyTag, new ByteArrayInputStream(new byte[0]), 0);
        }

        return responseList;
    }


    @SneakyThrows
    private StorageOperationResponse uploadFile(MultipartFile file, String fullPathToFolder, String folder) {
        try (InputStream stream = file.getInputStream()) {
            String objectPath = fullPathToFolder + file.getOriginalFilename();
            repository.safeUpload(objectPath, stream, file.getSize());

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

        List<StorageObjectResponse> allObjectsInFolder = repository.allObjectsInFolder(fullPathToFolder + innerFolderName);
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
                repository.forceUpload(fullPathToFolder + file.getOriginalFilename(), stream, file.getSize());
            }
        }
        return StorageOperationResponse.builder()
                .status(CREATED.value())
                .title(CREATED.getReasonPhrase())
                .detail("Папка '%s' успешно загружена".formatted(innerFolderName))
                .path(baseFolder + innerFolderName)
                .build();
    }

    private long getUsedMemory(Long userId) {
        return repository.objectStats(userId.toString() + "/")
                .map(StorageObjectResponse::getSize)
                .orElse(0L);
    }

    private static Set<String> getAllSubPaths(String path) {
        Set<String> subPaths = new HashSet<>();
        StringBuilder currentPath = new StringBuilder();

        String[] parts = path.split("/");

        for (int i = 0; i < parts.length - 1; i++) {
            currentPath.append(parts[i]).append("/");
            subPaths.add(currentPath.toString());
        }

        return subPaths;
    }

    private static String getFullPath(Long userId, String folderPath) {
        return userId.toString() + "/" + (folderPath == null ? "" : folderPath);
    }
}
