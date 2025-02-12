package org.mrshoffen.cloudstorage.storage.repository;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.catalina.connector.Response;
import org.mrshoffen.cloudstorage.storage.dto.StorageObject;
import org.mrshoffen.cloudstorage.storage.dto.StorageObjectDto;
import org.mrshoffen.cloudstorage.storage.exception.ConflictFileNameException;
import org.mrshoffen.cloudstorage.storage.exception.FileNotFoundException;
import org.mrshoffen.cloudstorage.storage.exception.MinioStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

        minioClient.removeObjects(
                        RemoveObjectsArgs.builder()
                                .bucket(bucket)
                                .objects(listForDelete)
                                .build()
                )
                .forEach(del -> {
                });
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

    public StorageObjectDto downloadFile(String fullFilePath) {
        try {
            InputStream stream = getFile(fullFilePath);

            StatObjectResponse fileStats = fileStats(fullFilePath);

            return StorageObjectDto.builder()
                    .inputStream(stream)
                    .size(fileStats.size())
                    .name(extractSimpleName(fullFilePath))
                    .build();

        } catch (ErrorResponseException e) {
            if (e.response().code() == Response.SC_NOT_FOUND) {
                throw new FileNotFoundException("'%s' не существует в исходной папке".formatted(extractSimpleName(fullFilePath)), e);
            }
            throw new MinioStorageException("Ошибка в хранилище файлов", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream getFile(String fullFilePath) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(fullFilePath)
                        .build()
        );
    }

    private long folderSize(String folderPath) {

        return getFilesWithPrefix(folderPath, true)
                .stream()
                .map(Item::size)
                .reduce(0L, Long::sum);

    }


    @SneakyThrows
    public StorageObjectDto downloadFolder(String folderPath) {
        try {
            PipedInputStream pipedInputStream = new PipedInputStream();
            PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);

            new Thread(() -> {
                try (ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(pipedOutputStream, 8192))) {
                    List<String> objectNames = getFilesWithPrefix(folderPath, true).stream()
                            .map(Item::objectName).toList();

                    long totalBytesWritten = 0;
                    for (String objectName : objectNames) {
                        try (InputStream inputStream = getFile(objectName)) {
                            ZipEntry zipEntry = new ZipEntry(objectName.replace(folderPath, ""));
                            zipOut.putNextEntry(zipEntry);

                            byte[] buffer = new byte[8192];
                            int len;
                            while ((len = inputStream.read(buffer)) > 0) {
                                zipOut.write(buffer, 0, len);
                                totalBytesWritten += len;
                                // Логирование прогресса (опционально)
                                System.out.println("Записано байт: " + totalBytesWritten);
                            }

                            zipOut.closeEntry();
                        } catch (Exception e) {
                            throw new RuntimeException("Ошибка при добавлении файла в архив: " + objectName, e);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Ошибка при создании архива", e);
                } finally {
                    try {
                        pipedOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            String zipName = extractSimpleName(folderPath).replace("/", ".zip");
            return StorageObjectDto.builder()
                    .inputStream(pipedInputStream)
                    .size(folderSize(folderPath))
                    .name(zipName)
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Ошибка при создании потоков", e);
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
            fileStats(fullFilePath);
            return true;
        } catch (MinioException e) {
            return false;
        }
    }

    private StatObjectResponse fileStats(String fullFilePath) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
        return minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucket)
                        .object(fullFilePath)
                        .build());
    }

    public boolean folderExists(String fullFolderPath) {
        List<Item> objects = getFilesWithPrefix(fullFolderPath, false);

        return !objects.isEmpty();
    }


    public List<StorageObject> getFolderItems(String fullPathToFolder) {
        return getStorageObjectsWithPrefix(fullPathToFolder, false);
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
                                Item item = result.get();

                                return item;
                            } catch (Exception e) {
                                throw new RuntimeException("Error occurred while getting items from folder " + prefix, e);
                            }
                        }
                )
                .toList();
    }

    private List<StorageObject> getStorageObjectsWithPrefix(String prefix, boolean recursive) {
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
                                Item item = result.get();


                                String absolutePath = item.objectName();

                                int lastSlashIndex = absolutePath.lastIndexOf('/', absolutePath.length() - 2);
                                String simpleName = absolutePath.substring(lastSlashIndex + 1);

                                int firstSlashIndex = absolutePath.indexOf('/');
                                String relativePath = absolutePath.substring(firstSlashIndex + 1);


                                return StorageObject.builder()
                                        .name(simpleName)
                                        .path(relativePath)
                                        .isFolder(item.isDir())
                                        .size(item.isDir() ? folderSize(item.objectName()) : item.size())
                                        .lastModified(item.lastModified())
                                        .build();
                            } catch (Exception e) {
                                throw new RuntimeException("Error occurred while getting items from folder " + prefix, e);
                            }
                        }
                ).toList();

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

    public static String extractSimpleName(String fullPath) {
        int lastSlashIndex = fullPath.lastIndexOf('/', fullPath.length() - 2);
        return fullPath.substring(lastSlashIndex + 1);
    }


    private void checkFolderExistsConflict(String folderPath) {
        if (!folderExists(folderPath)) {
            throw new FileNotFoundException("'%s' не существует в исходной папке".formatted(extractSimpleName(folderPath)));
        }
    }

}