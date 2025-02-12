package org.mrshoffen.cloudstorage.storage.repository;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.mrshoffen.cloudstorage.storage.model.StorageObject;
import org.mrshoffen.cloudstorage.storage.model.dto.response.StorageObjectResourceDto;
import org.mrshoffen.cloudstorage.storage.exception.ConflictFileNameException;
import org.mrshoffen.cloudstorage.storage.exception.FileNotFoundException;
import org.mrshoffen.cloudstorage.storage.minio.MinioOperationResolver;
import org.mrshoffen.cloudstorage.storage.minio.MinioOperations;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MinioRepository implements StorageObjectRepository {

    private final MinioOperationResolver operationResolver;

    @Override
    @SneakyThrows
    public List<StorageObject> find(String path) {
        return operationResolver.resolve(path)
                .findObjectWithPrefix(path);
    }

    @Override
    public StorageObjectResourceDto download(String path) {
        MinioOperations operations = operationResolver.resolve(path);

        ensureObjectExists(path, operations);

        return operations.getObjectAsResource(path);
    }


    @Override
    public void copy(String sourcePath, String targetPath) {
        MinioOperations operations = operationResolver.resolve(sourcePath);

        ensureObjectExists(sourcePath, operations);
        ensureObjectNotExists(targetPath, operations);

        operations.copyObject(sourcePath, targetPath);
    }


    @Override
    @SneakyThrows
    public void delete(String deletePath) {
        MinioOperations operations = operationResolver.resolve(deletePath);

        ensureObjectExists(deletePath, operations);

        operations.deleteObjectByPath(deletePath);
    }

    @Override
    @SneakyThrows
    public void move(String sourcePath, String targetPath) {
        MinioOperations operations = operationResolver.resolve(sourcePath);

        ensureObjectExists(sourcePath, operations);
        ensureObjectNotExists(targetPath, operations);

        operations.copyObject(sourcePath, targetPath);
        operations.deleteObjectByPath(sourcePath);
    }


    private void ensureObjectExists(String path, MinioOperations operations) {
        if (!operations.objectExists(path)) {
            throw new FileNotFoundException("'%s' не существует в исходной папке"
                    .formatted(extractSimpleName(path)));
        }
    }

    private void ensureObjectNotExists(String path, MinioOperations operations) {
        if (operations.objectExists(path)) {
            throw new ConflictFileNameException("'%s' уже существует в целевой папке"
                    .formatted(extractSimpleName(path)));

        }
    }

    protected static String extractSimpleName(String fullPath) {
        int lastSlashIndex = fullPath.lastIndexOf('/', fullPath.length() - 2);
        return fullPath.substring(lastSlashIndex + 1);
    }

}