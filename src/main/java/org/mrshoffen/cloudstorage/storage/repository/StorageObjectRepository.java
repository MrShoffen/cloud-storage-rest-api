package org.mrshoffen.cloudstorage.storage.repository;

import org.mrshoffen.cloudstorage.storage.model.StorageObject;
import org.mrshoffen.cloudstorage.storage.model.dto.response.StorageObjectResourceDto;

import java.io.InputStream;
import java.util.List;

public interface StorageObjectRepository {

    String getLinkForObject(String objectPath, int timeout);

    StorageObject objectStats(String objectPath);

    void uploadObject(String objectPath, InputStream inputStream, long size, boolean overwrite);

    List<StorageObject> findAllObjectsInFolder(String path);

    StorageObjectResourceDto getObject(String path);

    void copyObject(String sourcePath, String targetPath);

    void deleteObject(String deletePath);

    void moveObject(String sourcePath, String targetPath);

}
