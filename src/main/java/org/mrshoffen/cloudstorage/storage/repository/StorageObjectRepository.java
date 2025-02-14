package org.mrshoffen.cloudstorage.storage.repository;

import org.mrshoffen.cloudstorage.storage.model.StorageObject;
import org.mrshoffen.cloudstorage.storage.model.dto.response.StorageObjectResourceDto;

import java.io.InputStream;
import java.util.List;

public interface StorageObjectRepository {

    void uploadSingleObject(String objectPath, InputStream inputStream, long size);

    List<StorageObject> findAllObjectsInFolder(String path);

    StorageObjectResourceDto getObject(String path);

    void copyObject(String sourcePath, String targetPath);

    void deleteObject(String deletePath);

    void moveObject(String sourcePath, String targetPath);

}
