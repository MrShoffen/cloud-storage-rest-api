package org.mrshoffen.cloudstorage.storage.repository;

import org.mrshoffen.cloudstorage.storage.exception.StorageObjectAlreadyExistsException;
import org.mrshoffen.cloudstorage.storage.exception.StorageObjectNotFoundException;
import org.mrshoffen.cloudstorage.storage.model.dto.response.StorageObjectResponse;
import org.mrshoffen.cloudstorage.storage.model.dto.response.StorageObjectResourceDto;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public interface StorageObjectRepository {

    String objectDownloadLink(String objectPath)
            throws StorageObjectNotFoundException;

    Optional<StorageObjectResponse> objectStats(String objectPath);

    void forceUpload(String objectPath, InputStream inputStream, long size);

    void safeUpload(String objectPath, InputStream inputStream, long size)
            throws StorageObjectAlreadyExistsException;

    List<StorageObjectResponse> allObjectsInFolder(String path);

    StorageObjectResourceDto getAsResource(String path)
            throws StorageObjectNotFoundException;

    void copy(String sourcePath, String targetPath)
            throws StorageObjectNotFoundException, StorageObjectAlreadyExistsException;

    void delete(String deletePath)
            throws StorageObjectNotFoundException;

    void move(String sourcePath, String targetPath)
            throws StorageObjectNotFoundException, StorageObjectAlreadyExistsException;

}
