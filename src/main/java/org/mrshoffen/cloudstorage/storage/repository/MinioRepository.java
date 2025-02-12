package org.mrshoffen.cloudstorage.storage.repository;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.mrshoffen.cloudstorage.storage.dto.StorageObject;
import org.mrshoffen.cloudstorage.storage.dto.StorageObjectResourceDto;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MinioRepository {

    private final MinioOperationResolver operationResolver;

    @SneakyThrows
    public List<StorageObject> find(String path) {
        return operationResolver.resolveOperation(path)
                .findObjectWithPrefix(path);
    }

    public StorageObjectResourceDto download(String path) {
        return operationResolver.resolveOperation(path)
                .downloadObject(path);
    }


    public void copy(String sourcePath, String targetPath) {
        operationResolver.resolveOperation(sourcePath)
                .copyObject(sourcePath, targetPath);
    }


    @SneakyThrows
    public void delete(String deletePath) {
        operationResolver.resolveOperation(deletePath)
                .deleteObjectByPath(deletePath);

    }

    @SneakyThrows
    public void move(String sourcePath, String targetPath) {
        operationResolver.resolveOperation(sourcePath)
                .moveObject(sourcePath, targetPath);
    }

}