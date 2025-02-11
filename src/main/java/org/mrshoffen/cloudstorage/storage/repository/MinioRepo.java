package org.mrshoffen.cloudstorage.storage.repository;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
public abstract class MinioRepo {

    protected String bucket;

    protected final MinioClient minioClient;

    public abstract void deleteObject(String path);

    abstract void copyObject(String sourcePath, String targetPath);

    abstract boolean objectExists(String path);

    public List<Item> getFolderItems(String fullPathToFolder) {
        return getFilesWithPrefix(fullPathToFolder, false);
    }

    protected List<Item> getFilesWithPrefix(String prefix, boolean recursive) {
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
