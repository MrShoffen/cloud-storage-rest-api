package org.mrshoffen.cloudstorage.storage.minio;

import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;
import lombok.SneakyThrows;
import org.apache.catalina.connector.Response;
import org.mrshoffen.cloudstorage.storage.model.dto.response.StorageObjectResourceDto;
import org.mrshoffen.cloudstorage.storage.exception.FileNotFoundException;
import org.mrshoffen.cloudstorage.storage.exception.MinioStorageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
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


    @Override
    public StorageObjectResourceDto getObjectAsResource(String downloadPath) {
        try {
            InputStream stream = getFileStream(downloadPath);

            return StorageObjectResourceDto.builder()
                    .downloadResource(new InputStreamResource(stream))
                    .nameForSave(extractSimpleName(downloadPath))
                    .build();

        } catch (ErrorResponseException e) {
            if (e.response().code() == Response.SC_NOT_FOUND) {
                throw new FileNotFoundException("'%s' не существует в исходной папке".formatted(extractSimpleName(downloadPath)), e);
            }
            throw new MinioStorageException("Ошибка в хранилище файлов", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
