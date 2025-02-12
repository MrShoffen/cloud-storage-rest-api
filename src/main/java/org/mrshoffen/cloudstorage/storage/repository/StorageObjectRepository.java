package org.mrshoffen.cloudstorage.storage.repository;

import org.mrshoffen.cloudstorage.storage.model.StorageObject;
import org.mrshoffen.cloudstorage.storage.model.dto.response.StorageObjectResourceDto;

import java.util.List;

public interface StorageObjectRepository {

    List<StorageObject> find(String path);

    StorageObjectResourceDto download(String path);

    void copy(String sourcePath, String targetPath);

    void delete(String deletePath);

    void move(String sourcePath, String targetPath);

}
