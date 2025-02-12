package org.mrshoffen.cloudstorage.storage.minio;

import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.storage.model.StorageObject;
import org.mrshoffen.cloudstorage.storage.model.dto.response.StorageObjectResourceDto;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
public abstract class MinioOperations {

    protected final String bucket;

    protected final MinioClient minioClient;

    public abstract void deleteObjectByPath(String path);

    public abstract void copyObject(String sourcePath, String targetPath);

    public abstract StorageObjectResourceDto getObjectAsResource(String folderPath);

    public boolean objectExists(String path) {
        return !findItemsWithPrefix(path, false)
                .isEmpty();
    }

    public List<StorageObject> findObjectWithPrefix(String fullPathToFolder) {
        List<Item> items = findItemsWithPrefix(fullPathToFolder, false);

        return items.stream()
                .map(item -> {
                    Long size = getItemSize(item);

                    return StorageObject.builder()
                            .name(extractSimpleName(item.objectName()))
                            .path(extractRelativePath(item.objectName()))
                            .isFolder(item.isDir())
                            .lastModified(item.lastModified())
                            .size(size)
                            .build();
                })
                .toList();

    }


    protected List<Item> findItemsWithPrefix(String prefix, boolean recursive) {
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
                        throw new RuntimeException(e);
                    }
                })
                .toList();

    }

    private Long getItemSize(Item item) {
        if (item.isDir()) {
            return findItemsWithPrefix(item.objectName(), true)
                    .stream()
                    .map(Item::size)
                    .reduce(0L, Long::sum);
        } else {
            return item.size();
        }
    }

    protected InputStream getFileStream(String fullFilePath) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(fullFilePath)
                        .build()
        );
    }

    protected static String extractSimpleName(String fullPath) {
        int lastSlashIndex = fullPath.lastIndexOf('/', fullPath.length() - 2);
        return fullPath.substring(lastSlashIndex + 1);
    }

    private static String extractRelativePath(String fullPath) {
        int firstSlashIndex = fullPath.indexOf('/');
        return fullPath.substring(firstSlashIndex + 1);
    }

 }
