package org.mrshoffen.cloudstorage.storage.repository;

import org.mrshoffen.cloudstorage.storage.minio.MinioCacheService;
import org.mrshoffen.cloudstorage.storage.exception.StorageObjectAlreadyExistsException;
import org.mrshoffen.cloudstorage.storage.exception.StorageObjectNotFoundException;
import org.mrshoffen.cloudstorage.storage.minio.MinioOperationResolver;
import org.mrshoffen.cloudstorage.storage.model.dto.response.StorageObjectResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Repository
public class CachedMinioRepository extends MinioRepository {

    @Value("${minio.cache.presigned-timeout}")
    private int presignedLinkTimeout;

    @Value("${minio.cache.folder-content-timeout}")
    private int folderContentTimeout;

    private final MinioCacheService cacheService;

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
        if (!cachedList.isEmpty()) {
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

    @Override
    public void copy(String sourcePath, String targetPath) throws StorageObjectNotFoundException, StorageObjectAlreadyExistsException {
        String folderWithTargetObject = extractObjectFolder(targetPath);
        cacheService.deleteFolderContent(folderWithTargetObject);

        super.copy(sourcePath, targetPath);
    }

    @Override
    public void move(String sourcePath, String targetPath) throws StorageObjectNotFoundException, StorageObjectAlreadyExistsException {
        String folderWithTargetObject = extractObjectFolder(targetPath);
        cacheService.deleteFolderContent(folderWithTargetObject);

        String folderWithSourceObject = extractObjectFolder(sourcePath);
        cacheService.deleteFolderContent(folderWithSourceObject);

        super.move(sourcePath, targetPath);
    }

    @Override
    public void forceUpload(String objectPath, InputStream inputStream, long size) {
        Set<String> allSubPaths = getAllSubPaths(objectPath);
        for (String subPath : allSubPaths) {
            cacheService.deleteFolderContent(subPath);
        }

        super.forceUpload(objectPath, inputStream, size);
    }

    @Override
    public void safeUpload(String objectPath, InputStream inputStream, long size) throws StorageObjectAlreadyExistsException {
        Set<String> allSubPaths = getAllSubPaths(objectPath);
        for (String subPath : allSubPaths) {
            cacheService.deleteFolderContent(subPath);
        }

        super.safeUpload(objectPath, inputStream, size);
    }

    private static String extractObjectFolder(String fullPath) {
        int lastSlashIndex = fullPath.lastIndexOf('/', fullPath.length() - 2);
        return fullPath.substring(0, lastSlashIndex + 1);
    }

    private static Set<String> getAllSubPaths(String path) {
        Set<String> subPaths = new HashSet<>();
        StringBuilder currentPath = new StringBuilder();

        String[] parts = path.split("/");

        for (int i = 0; i < parts.length - 1; i++) {
            currentPath.append(parts[i]).append("/");
            subPaths.add(currentPath.toString());
        }

        return subPaths;
    }
}
