package org.mrshoffen.cloudstorage.storage.repository;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.catalina.connector.Response;
import org.mrshoffen.cloudstorage.storage.exception.ConflictFileNameException;
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


    public void deleteDirectory(String folderDeletePath) {
        checkFolderExistsConflict(folderDeletePath);

        List<DeleteObject> listForDelete = getFilesWithPrefix(folderDeletePath, true)
                .stream()
                .map(Item::objectName)
                .map(DeleteObject::new)
                .toList();

        minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                        .bucket(bucket)
                        .objects(listForDelete)
                        .build()
        );
    }


    @SneakyThrows
    public void deleteFile(String fileDeletePath) {
        if (!fileExists(fileDeletePath)) {
            throw new FileNotFoundException("'%s' не существует в исходной папке".formatted(fileDeletePath));
        }
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(fileDeletePath)
                        .build()
        );
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
                throw new FileNotFoundException("'%s' не существует в исходной папке".formatted(extractSimpleName(fullFilePath)), e);
            }
            throw new MinioStorageException("Ошибка в хранилище файлов", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    public void copyDirectory(String fullTargetPath, String fullSourcePath) {
        checkFolderConflict(fullTargetPath);
        checkFolderExistsConflict(fullSourcePath);

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
        checkFileConflict(fullTargetPath);

        try {
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
        } catch (ErrorResponseException e) {
            if (e.response().code() == Response.SC_NOT_FOUND) {
                throw new FileNotFoundException("'%s' не существует в исходной папке".formatted(extractSimpleName(fullTargetPath)), e);
            }
            throw new MinioStorageException("Ошибка в хранилище файлов", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }//todo override to 2 functions


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


    public List<Item> getFolderItems(String fullPathToFolder) {
        return getFilesWithPrefix(fullPathToFolder, false);
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
                                throw new RuntimeException("Error occurred while getting items from folder " + prefix, e);
                            }
                        }
                )
                .toList();
    }

    private void checkFolderConflict(String fullFolderPath) {
        if (folderExists(fullFolderPath)) {
            throw new ConflictFileNameException("'%s' уже существует в целевой папке"
                    .formatted(extractSimpleName(fullFolderPath)));
        }
    }

    @SneakyThrows
    private void checkFileConflict(String fullFilePath) {
        if (fileExists(fullFilePath)) {
            throw new ConflictFileNameException("'%s' уже существует в целевой папке"
                    .formatted(extractSimpleName(fullFilePath)));
        }
    }

    static String extractSimpleName(String fullPath) {
        int lastSlashIndex = fullPath.lastIndexOf('/', fullPath.length() - 2);
        return fullPath.substring(lastSlashIndex + 1);
    }


    private void checkFolderExistsConflict(String folderPath) {
        if (!folderExists(folderPath)) {
            throw new FileNotFoundException("'%s' не существует в исходной папке".formatted(extractSimpleName(folderPath)));
        }
    }

}