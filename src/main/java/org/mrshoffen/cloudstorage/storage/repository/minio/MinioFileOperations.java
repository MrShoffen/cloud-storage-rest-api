package org.mrshoffen.cloudstorage.storage.repository.minio;

import io.minio.*;
import io.minio.errors.*;
import lombok.SneakyThrows;
import org.apache.catalina.connector.Response;
import org.mrshoffen.cloudstorage.storage.dto.StorageObjectDto;
import org.mrshoffen.cloudstorage.storage.dto.StorageObjectResourceDto;
import org.mrshoffen.cloudstorage.storage.exception.FileNotFoundException;
import org.mrshoffen.cloudstorage.storage.exception.MinioStorageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class MinioFileOperations extends MinioOperations {

    @Autowired
    public MinioFileOperations(MinioClient minioClient,
                               @Value("${minio.bucket-name}") String bucketName) {
        super(bucketName, minioClient);
    }


    @Override
    @SneakyThrows
    public void deleteObjectByPath(String path) {
        ensureObjectExists(path);
        delete(path);
    }

    @Override
    @SneakyThrows
    public void copyObject(String sourcePath, String targetPath) {
        ensureObjectExists(sourcePath);
        ensureObjectNotExists(targetPath);
        copy(sourcePath, targetPath);
    }

    @Override
    @SneakyThrows
    public void moveObject(String sourcePath, String targetPath) {
        ensureObjectExists(sourcePath);
        ensureObjectNotExists(targetPath);

        copy(sourcePath, targetPath);
        delete(sourcePath);
    }

    @Override
    public StorageObjectResourceDto downloadObject(String downloadPath) {
        try {
            InputStream stream = getFile(downloadPath);

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

    private void delete(String sourcePath) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(sourcePath)
                        .build()
        );
    }

    private void copy(String sourcePath, String targetPath) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
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
}
