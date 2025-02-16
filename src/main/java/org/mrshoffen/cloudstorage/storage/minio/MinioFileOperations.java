package org.mrshoffen.cloudstorage.storage.minio;

import io.minio.*;
import io.minio.errors.MinioException;
import lombok.SneakyThrows;
import org.mrshoffen.cloudstorage.storage.model.StorageObjectStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class MinioFileOperations extends MinioOperations {

    @Autowired
    public MinioFileOperations(MinioClient minioClient,
                               @Value("${minio.bucket-name}") String bucketName) {
        super(bucketName, minioClient);
    }

    @Override
    @SneakyThrows
    public StorageObjectStats objectStats(String fullPath) {
        StatObjectResponse response = minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucket)
                        .object(fullPath)
                        .build()
        );

        return StorageObjectStats.builder()
                .name(extractSimpleName(response.object()))
                .path(extractRelativePath(response.object()))
                .isFolder(false)
                .size(response.size())
                .lastModified(response.lastModified())
                .build();
    }

    @Override
    @SneakyThrows
    public void deleteObjectByPath(String path) {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(path)
                        .build()
        );
    }

    @Override
    @SneakyThrows
    public void copyObject(String sourcePath, String targetPath) {
        minioClient.copyObject(
                CopyObjectArgs.builder()
                        .bucket(bucket)
                        .object(targetPath)
                        .source(
                                CopySource.builder()
                                        .bucket(bucket)
                                        .object(sourcePath)
                                        .build()
                        )
                        .build()
        );
    }

    @SneakyThrows
    @Override
    public InputStream readObject(String downloadPath) {
            return getFileStream(downloadPath);
    }

    @SneakyThrows
    @Override
    public boolean objectExists(String path) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(path)
                            .build());
            return true;
        } catch (MinioException e) {
            return false;
        }
    }

}
