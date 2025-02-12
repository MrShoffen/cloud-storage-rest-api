package org.mrshoffen.cloudstorage.storage.repository.minio;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.storage.dto.StorageObject;
import org.mrshoffen.cloudstorage.storage.exception.ConflictFileNameException;
import org.mrshoffen.cloudstorage.storage.exception.FileNotFoundException;
import org.mrshoffen.cloudstorage.storage.repository.StorageObjectService;

import java.util.List;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
public abstract class MinioService implements StorageObjectService {

    protected final String bucket;

    protected final MinioClient minioClient;


    @Override
    public List<StorageObject> findStorageObjectsWithPrefix(String fullPathToFolder) {
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

    protected void ensureObjectExists(String path) {
        if (!objectExists(path)) {
            throw new FileNotFoundException("'%s' не существует в исходной папке"
                    .formatted(extractSimpleName(path)));
        }
    }

    protected void ensureObjectNotExists(String path) {
        if (objectExists(path)) {
            throw new ConflictFileNameException("'%s' уже существует в целевой папке"
                    .formatted(extractSimpleName(path)));

        }
    }

    private boolean objectExists(String path) {
        return !findItemsWithPrefix(path, false)
                .isEmpty();
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


    private static String extractSimpleName(String fullPath) {
        int lastSlashIndex = fullPath.lastIndexOf('/', fullPath.length() - 2);
        return fullPath.substring(lastSlashIndex + 1);
    }

    private static String extractRelativePath(String fullPath) {
        int firstSlashIndex = fullPath.indexOf('/');
        return fullPath.substring(firstSlashIndex + 1);
    }
}
