package org.mrshoffen.cloudstorage.storage.repository.minio;

import io.minio.*;
import io.minio.errors.*;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class MinioFileService extends MinioService {

    @Autowired
    public MinioFileService(MinioClient minioClient,
                            @Value("${minio.bucket-name}") String bucketName) {
        super(bucketName, minioClient);
    }


    @Override
    @SneakyThrows
    public void deleteStorageObject(String path) {
        ensureObjectExists(path);
        delete(path);
    }

    @Override
    @SneakyThrows
    public void copyStorageObject(String sourcePath, String targetPath) {
        ensureObjectExists(sourcePath);
        ensureObjectNotExists(targetPath);
        copy(sourcePath, targetPath);
    }

    @Override
    @SneakyThrows
    public void moveStorageObject(String sourcePath, String targetPath) {
        ensureObjectExists(sourcePath);
        ensureObjectNotExists(targetPath);

        copy(sourcePath, targetPath);
        delete(sourcePath);
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
