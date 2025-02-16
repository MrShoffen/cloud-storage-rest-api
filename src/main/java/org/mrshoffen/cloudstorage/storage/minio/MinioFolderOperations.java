package org.mrshoffen.cloudstorage.storage.minio;

import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.MinioClient;
import io.minio.RemoveObjectsArgs;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import org.mrshoffen.cloudstorage.storage.model.dto.response.StorageObjectResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class MinioFolderOperations extends MinioOperations {

    @Autowired
    public MinioFolderOperations(MinioClient minioClient,
                                 @Value("${minio.bucket-name}") String bucketName) {
        super(bucketName, minioClient);
    }

    @Override
    public StorageObjectResponse objectStats(String fullPath) {
        List<Item> items = findItemsWithPrefix(fullPath, true);

        Long size = items.stream()
                .map(Item::size)
                .reduce(0L, Long::sum);

        ZonedDateTime lastModified = items.stream()
                .map(Item::lastModified)
                .sorted(ZonedDateTime::compareTo)
                .findFirst().get();

        return StorageObjectResponse.builder()
                .name(extractSimpleName(fullPath))
                .path(extractRelativePath(fullPath))
                .isFolder(true)
                .size(size)
                .lastModified(lastModified)
                .build();
    }

    @Override
    public void deleteObjectByPath(String path) {
        List<Item> allInnerItems = findItemsWithPrefix(path, true);

        List<DeleteObject> listForDelete = allInnerItems.stream()
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

    @Override
    @SneakyThrows
    public void copyObject(String sourcePath, String targetPath) {
        List<Item> allInnerSourceItems = findItemsWithPrefix(sourcePath, true);

        for (Item item : allInnerSourceItems) {
            String innerObjectSourcePath = item.objectName();
            String innerObjectTargetPath = innerObjectSourcePath.replaceFirst(sourcePath, targetPath);
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucket)
                            .object(innerObjectTargetPath)
                            .source(
                                    CopySource.builder()
                                            .bucket(bucket)
                                            .object(innerObjectSourcePath)
                                            .build()
                            )
                            .build()
            );
        }
    }


    @Override
    public InputStream readObject(String downloadPath) {
        try {
            PipedInputStream pipedInputStream = new PipedInputStream();
            PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);

            new Thread(() -> {
                try (ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(pipedOutputStream, 8192))) {
                    List<String> objectNames = findItemsWithPrefix(downloadPath, true).stream()
                            .map(Item::objectName).toList();

                    for (String objectName : objectNames) {
                        try (InputStream inputStream = getFileStream(objectName)) {
                            ZipEntry zipEntry = new ZipEntry(objectName.replace(downloadPath, ""));
                            zipOut.putNextEntry(zipEntry);

                            byte[] buffer = new byte[8192];
                            int len;
                            while ((len = inputStream.read(buffer)) > 0) {
                                zipOut.write(buffer, 0, len);
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

            return pipedInputStream;

        } catch (IOException e) {
            throw new RuntimeException("Ошибка при создании потоков", e);
        }
    }

    @Override
    public boolean objectExists(String path) {
        return !findItemsWithPrefix(path, false)
                .isEmpty();
    }
}
