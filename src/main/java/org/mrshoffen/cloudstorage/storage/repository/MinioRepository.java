package org.mrshoffen.cloudstorage.storage.repository;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.catalina.connector.Response;
import org.mrshoffen.cloudstorage.storage.exception.FileNotFoundException;
import org.mrshoffen.cloudstorage.storage.exception.MinioStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.List;
import java.util.stream.StreamSupport;

@Repository
@RequiredArgsConstructor
public class MinioRepository {
    @Value("${minio.bucket-name}")
    private String bucket;

    private final MinioClient minioClient;

    @SneakyThrows
    public void deleteDirectory(String folderDeletePath) {
        List<DeleteObject> listForDelete = getFilesWithPrefix(folderDeletePath, true)
                .stream()
                .map(Item::objectName)
                .map(DeleteObject::new)
                .toList();

//todo catch exception - if resource doesnt exist
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                        .bucket(bucket)
                        .objects(listForDelete)
                        .build()
        );

        for (Result<DeleteError> result : results) {
            DeleteError error = result.get();
            String x = "Error in deleting object " + error.objectName() + "; " + error.message();
            System.out.println(
                    x);
        }

    }

    @SneakyThrows
    public void deleteFile(String fileDeletePath) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(fileDeletePath)
                            .build()
            );
        } catch (Exception e) {

            System.out.println();
        }

    }

    public InputStream downloadFile(String fullFilePath) {
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(fullFilePath)
                            .build()
            );

            return stream;
        } catch (ErrorResponseException e) {
            if (e.response().code() == Response.SC_NOT_FOUND) {
                throw new FileNotFoundException("'%s' не найден".formatted(fullFilePath), e);
            }
            throw new MinioStorageException("Ошибка в хранилище файлов", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    public void copyDirectory(String fullTargetPath, String fullSourcePath) {
        getFilesWithPrefix(fullSourcePath, true)
                .stream()
                .map(Item::objectName)
                .forEach(sourcePath -> {
                    String targetPath = sourcePath.replaceFirst(fullSourcePath, fullTargetPath);
                    copyFile(targetPath, sourcePath);
                });
    }

    @SneakyThrows
    public void copyFile(String fullTargetPath, String fullSourcePath) {
        minioClient.copyObject(
                CopyObjectArgs.builder()
                        .bucket(bucket)
                        .object(fullTargetPath)
                        .source(
                                CopySource.builder()
                                        .bucket(bucket)
                                        .object(fullSourcePath)
                                        .build()
                        )
                        .build()
        );
    }//todo override to 2 functions

    public List<Item> getFolderItems(String fullPathToFolder) {
        return getFilesWithPrefix(fullPathToFolder, false);
    }


    @SneakyThrows
    public boolean fileExists(String fullFilePath) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(fullFilePath)
                            .build());
            return true;
        } catch (MinioException e) {
            return false;
        }
    }

    public boolean folderExists(String fullFolderPath) {
        List<Item> objects = getFilesWithPrefix(fullFolderPath, false);

        return !objects.isEmpty();
    }


    private List<Item> getFilesWithPrefix(String prefix, boolean recursive) {
        Iterable<Result<Item>> objects = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .recursive(recursive)
                        .prefix(prefix)
                        .build()
        );

        return StreamSupport.stream(objects.spliterator(), false)
                .map(result -> {
                            try {
                                return result.get();
                            } catch (Exception e) {
                                //todo throw specified exception
                                throw new RuntimeException("Error occurred while getting items from folder " + prefix, e);
                            }
                        }
                )
                .toList();


    }

}