package org.mrshoffen.cloudstorage.storage.repository.minio;

import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.MinioClient;
import io.minio.RemoveObjectsArgs;
import io.minio.errors.*;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
public class MinioFolderService extends MinioService {

    @Autowired
    public MinioFolderService(MinioClient minioClient,
                              @Value("${minio.bucket-name}") String bucketName) {
        super(bucketName, minioClient);
    }


    @Override
    public void deleteStorageObject(String path) {
        ensureObjectExists(path);

        List<Item> allInnerItems = findItemsWithPrefix(path, true);
        delete(allInnerItems);
    }

    @Override
    @SneakyThrows
    public void copyStorageObject(String sourcePath, String targetPath) {
        ensureObjectExists(sourcePath);
        ensureObjectNotExists(targetPath);

        innerCopy(sourcePath, targetPath);
    }

    @Override
    @SneakyThrows
    public void moveStorageObject(String sourcePath, String targetPath) {
        ensureObjectExists(sourcePath);
        ensureObjectNotExists(targetPath);

        List<Item> allInnerSourceItems = innerCopy(sourcePath, targetPath);
        delete(allInnerSourceItems);
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
