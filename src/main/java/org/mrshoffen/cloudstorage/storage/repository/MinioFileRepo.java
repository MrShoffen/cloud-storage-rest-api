package org.mrshoffen.cloudstorage.storage.repository;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;

public class MinioFileRepo extends MinioRepo{
    public MinioFileRepo(MinioClient minioClient) {
        super(minioClient);
    }

    @Override
   public void deleteObject(String path) {

    }

    @Override
    void copyObject(String sourcePath, String targetPath) {

    }

    @Override
    boolean objectExists(String path) {
        return false;
    }
}
