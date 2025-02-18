package org.mrshoffen.cloudstorage.storage.repository;

import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.storage.exception.StorageDownloadException;
import org.mrshoffen.cloudstorage.storage.model.dto.response.StorageObjectResponse;
import org.mrshoffen.cloudstorage.storage.model.dto.response.StorageObjectResourceDto;
import org.mrshoffen.cloudstorage.storage.exception.StorageObjectAlreadyExistsException;
import org.mrshoffen.cloudstorage.storage.exception.StorageObjectNotFoundException;
import org.mrshoffen.cloudstorage.storage.minio.MinioOperationResolver;
import org.mrshoffen.cloudstorage.storage.minio.operations.MinioOperations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

//@Repository
@RequiredArgsConstructor
public class MinioRepository implements StorageObjectRepository {

    private final MinioOperationResolver operationResolver;

    @Value("${minio.cache.presigned-timeout}")
    private int presignedLinkTimeout;

    @Override
    public String objectDownloadLink(String objectPath) throws StorageObjectNotFoundException {
        MinioOperations operations = operationResolver.resolve(objectPath);
        ensureObjectExists(objectPath, operations);
        String presignedLink = operations.getPresignedLink(objectPath, presignedLinkTimeout);

        return presignedLink;
    }

    @Override
    public Optional<StorageObjectResponse> objectStats(String objectPath) {
        try {
            StorageObjectResponse storageObjectResponse = operationResolver.resolve(objectPath)
                    .objectStats(objectPath);
            return Optional.of(storageObjectResponse);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void forceUpload(String objectPath, InputStream inputStream, long size) {
        MinioOperations operations = operationResolver.resolve(objectPath);

        operations.putObject(objectPath, inputStream, size);
    }

    @Override
    public void safeUpload(String objectPath, InputStream inputStream, long size) throws StorageObjectAlreadyExistsException {
        MinioOperations operations = operationResolver.resolve(objectPath);

        ensureObjectNotExists(objectPath, operations);

        operations.putObject(objectPath, inputStream, size);
    }

    @Override
    public List<StorageObjectResponse> allObjectsInFolder(String path) {
        return operationResolver.resolve(path)
                .findObjectsWithPrefix(path);
    }

    @Override
    public List<StorageObjectResponse> findObjectsByName(String folderPath, String objectName) {
        return operationResolver.resolve(folderPath)
                .findObjectsWithPrefixRecursive(folderPath)
                .stream()
                .filter(storageObject ->
                        storageObject
                                .getName()
                                .toLowerCase()
                                .contains(objectName.toLowerCase()))
                .toList();
    }

    @Override
    public StorageObjectResourceDto getAsResource(String path) {
        MinioOperations operations = operationResolver.resolve(path);

        ensureObjectExists(path, operations);

        try {
            InputStream stream = operations.readObject(path);
            return StorageObjectResourceDto.builder()
                    .downloadResource(new InputStreamResource(stream))
                    .nameForSave(extractSimpleName(path))
                    .build();
        } catch (Exception e) {
            throw new StorageDownloadException(e);
        }
    }


    @Override
    public void copy(String sourcePath, String targetPath) throws StorageObjectNotFoundException, StorageObjectAlreadyExistsException {
        MinioOperations operations = operationResolver.resolve(sourcePath);

        ensureObjectExists(sourcePath, operations);
        ensureObjectNotExists(targetPath, operations);

        operations.copyObject(sourcePath, targetPath);
    }


    @Override
    public void delete(String deletePath) throws StorageObjectNotFoundException {
        MinioOperations operations = operationResolver.resolve(deletePath);

        ensureObjectExists(deletePath, operations);

        operations.deleteObjectByPath(deletePath);
    }

    @Override
    public void move(String sourcePath, String targetPath) throws StorageObjectNotFoundException, StorageObjectAlreadyExistsException {
        MinioOperations operations = operationResolver.resolve(sourcePath);

        ensureObjectExists(sourcePath, operations);
        ensureObjectNotExists(targetPath, operations);

        operations.copyObject(sourcePath, targetPath);
        operations.deleteObjectByPath(sourcePath);
    }


    private void ensureObjectExists(String path, MinioOperations operations) {
        if (!operations.objectExists(path)) {
            throw new StorageObjectNotFoundException("'%s' не существует в исходной папке"
                    .formatted(extractSimpleName(path)));
        }
    }

    private void ensureObjectNotExists(String path, MinioOperations operations) {
        if (operations.objectExists(path)) {
            throw new StorageObjectAlreadyExistsException("'%s' уже существует в целевой папке"
                    .formatted(extractSimpleName(path)));

        }
    }

    protected static String extractSimpleName(String fullPath) {
        int lastSlashIndex = fullPath.lastIndexOf('/', fullPath.length() - 2);
        return fullPath.substring(lastSlashIndex + 1);
    }

}