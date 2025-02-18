package org.mrshoffen.cloudstorage.storage.repository;

import org.mrshoffen.cloudstorage.storage.cache.MinioCacheService;
import org.mrshoffen.cloudstorage.storage.exception.StorageObjectNotFoundException;
import org.mrshoffen.cloudstorage.storage.minio.MinioOperationResolver;
import org.mrshoffen.cloudstorage.storage.model.dto.response.StorageObjectResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public class CachedMinioRepository extends MinioRepository {

    @Value("${minio.cache.presigned-timeout}")
    private int presignedLinkTimeout;

    @Value("${minio.cache.folder-content-timeout}")
    private int folderContentTimeout;

    private final MinioCacheService cacheService;


    @Autowired
    public CachedMinioRepository(MinioOperationResolver operationResolver, MinioCacheService presignedCacheService) {
        super(operationResolver);
        this.cacheService = presignedCacheService;
    }

    @Override
    public String objectDownloadLink(String objectPath) throws StorageObjectNotFoundException {
        String cachedLink = cacheService.getPresignedUrl(objectPath);
        if (cachedLink != null) {
            return cachedLink;
        }

        String newUrl = super.objectDownloadLink(objectPath);
        cacheService.savePresignedUrl(objectPath, newUrl, presignedLinkTimeout);
        return newUrl;
    }

    @Override
    public List<StorageObjectResponse> allObjectsInFolder(String path) {
        List<StorageObjectResponse> cachedList = cacheService.getFolderContent(path);
        if (cachedList != null) {
            return cachedList;
        }

        List<StorageObjectResponse> freshFolderContent = super.allObjectsInFolder(path);
        cacheService.saveFolderContent(path, freshFolderContent, folderContentTimeout);
        return freshFolderContent;
    }

    @Override
    public void delete(String deletePath) throws StorageObjectNotFoundException {
        String folderWithDeletedObject = extractObjectFolder(deletePath);
        cacheService.deleteFolderContent(folderWithDeletedObject);
        super.delete(deletePath);
    }

    private static String extractObjectFolder(String fullPath) {
        int lastSlashIndex = fullPath.lastIndexOf('/', fullPath.length() - 2);
        return fullPath.substring(0, lastSlashIndex + 1);
    }
}
