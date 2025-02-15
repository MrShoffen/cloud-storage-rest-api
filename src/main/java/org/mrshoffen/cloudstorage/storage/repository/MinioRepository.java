package org.mrshoffen.cloudstorage.storage.repository;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.mrshoffen.cloudstorage.storage.model.StorageObject;
import org.mrshoffen.cloudstorage.storage.model.dto.response.StorageObjectResourceDto;
import org.mrshoffen.cloudstorage.storage.exception.StorageObjectAlreadyExistsException;
import org.mrshoffen.cloudstorage.storage.exception.StorageObjectNotFoundException;
import org.mrshoffen.cloudstorage.storage.minio.MinioOperationResolver;
import org.mrshoffen.cloudstorage.storage.minio.MinioOperations;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MinioRepository implements StorageObjectRepository {

    private final MinioOperationResolver operationResolver;

    @Override
    public String getLinkForObject(String objectPath, int timeout) {
        MinioOperations operations = operationResolver.resolve(objectPath);

        ensureObjectExists(objectPath, operations);

        return operations.getPresignedLink(objectPath, timeout);
    }

    @Override
    public StorageObject objectStats(String objectPath) {
        return operationResolver.resolve(objectPath)
                .objectStats(objectPath);
    }

    @Override
    public void uploadObject(String objectPath, InputStream inputStream, long size, boolean overwrite) {
        MinioOperations operations = operationResolver.resolve(objectPath);

        if (!overwrite) {
            ensureObjectNotExists(objectPath, operations);
        }

        operations.putObject(objectPath, inputStream, size);
    }

    @Override
    @SneakyThrows
    public List<StorageObject> findAllObjectsInFolder(String path) {
        return operationResolver.resolve(path)
                .findObjectsWithPrefix(path);
    }

    @Override
    public StorageObjectResourceDto getObject(String path) {
        MinioOperations operations = operationResolver.resolve(path);

        ensureObjectExists(path, operations);

        InputStream stream = operations.readObject(path);

        return StorageObjectResourceDto.builder()
                .downloadResource(new InputStreamResource(stream))
                .nameForSave(extractSimpleName(path))
                .build();
    }


    @Override
    public void copyObject(String sourcePath, String targetPath) {
        MinioOperations operations = operationResolver.resolve(sourcePath);

        ensureObjectExists(sourcePath, operations);
        ensureObjectNotExists(targetPath, operations);

        operations.copyObject(sourcePath, targetPath);
    }


    @Override
    @SneakyThrows
    public void deleteObject(String deletePath) {
        MinioOperations operations = operationResolver.resolve(deletePath);

        ensureObjectExists(deletePath, operations);

        operations.deleteObjectByPath(deletePath);
    }

    @Override
    @SneakyThrows
    public void moveObject(String sourcePath, String targetPath) {
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