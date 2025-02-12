package org.mrshoffen.cloudstorage.storage.repository;

import org.mrshoffen.cloudstorage.storage.dto.StorageObject;

import java.util.List;

public interface StorageObjectService {
    void deleteStorageObject(String path);

    void copyStorageObject(String sourcePath, String targetPath);

    void moveStorageObject(String sourcePath, String targetPath);

    List<StorageObject> findStorageObjectsWithPrefix(String fullPathToFolder);
}
