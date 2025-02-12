package org.mrshoffen.cloudstorage.storage.repository;


import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.storage.repository.minio.MinioFileService;
import org.mrshoffen.cloudstorage.storage.repository.minio.MinioFolderService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StorageObjectFactory {

    private final MinioFolderService minioFolderService;
    private final MinioFileService minioFileService;


    public StorageObjectService getService(String sourcePath) {
        if (isFolderPath(sourcePath)) {
            return minioFolderService;
        } else {
            return minioFileService;
        }
    }


    static boolean isFolderPath(String fullTargetPath) {
        return fullTargetPath.endsWith("/");
    }

}
