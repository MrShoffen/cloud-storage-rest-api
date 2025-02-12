package org.mrshoffen.cloudstorage.storage.repository.minio;

import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.MinioClient;
import io.minio.RemoveObjectsArgs;
import io.minio.errors.*;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import org.mrshoffen.cloudstorage.storage.dto.StorageObjectDto;
import org.mrshoffen.cloudstorage.storage.dto.StorageObjectResourceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class MinioFolderOperations extends MinioOperations {

    @Autowired
    public MinioFolderOperations(MinioClient minioClient,
                                 @Value("${minio.bucket-name}") String bucketName) {
        super(bucketName, minioClient);
    }


    @Override
    public void deleteObjectByPath(String path) {
        ensureObjectExists(path);

        List<Item> allInnerItems = findItemsWithPrefix(path, true);
        delete(allInnerItems);
    }

    @Override
    @SneakyThrows
    public void copyObject(String sourcePath, String targetPath) {
        ensureObjectExists(sourcePath);
        ensureObjectNotExists(targetPath);

        innerCopy(sourcePath, targetPath);
    }

    @Override
    @SneakyThrows
    public void moveObject(String sourcePath, String targetPath) {
        ensureObjectExists(sourcePath);
        ensureObjectNotExists(targetPath);

        List<Item> allInnerSourceItems = innerCopy(sourcePath, targetPath);
        delete(allInnerSourceItems);
    }

    @Override
    public StorageObjectResourceDto downloadObject(String downloadPath) {
        try {
            PipedInputStream pipedInputStream = new PipedInputStream();
            PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);

            new Thread(() -> {
                try (ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(pipedOutputStream, 8192))) {
                    List<String> objectNames = findItemsWithPrefix(downloadPath, true).stream()
                            .map(Item::objectName).toList();

                    long totalBytesWritten = 0;
                    for (String objectName : objectNames) {
                        try (InputStream inputStream = getFile(objectName)) {
                            ZipEntry zipEntry = new ZipEntry(objectName.replace(downloadPath, ""));
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

            String zipName = extractSimpleName(downloadPath).replace("/", ".zip");
            return StorageObjectResourceDto.builder()
                    .downloadResource(new InputStreamResource(pipedInputStream))
                    .nameForSave(zipName)
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Ошибка при создании потоков", e);
        }
    }


    private List<Item> innerCopy(String sourcePath, String targetPath) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
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

        return allInnerSourceItems;
    }

    private void delete(List<Item> itemList) {

        List<DeleteObject> listForDelete = itemList.stream()
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
}
