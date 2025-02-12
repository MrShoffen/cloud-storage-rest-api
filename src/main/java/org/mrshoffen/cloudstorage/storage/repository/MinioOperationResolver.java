package org.mrshoffen.cloudstorage.storage.repository;


import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.storage.repository.minio.MinioFileOperations;
import org.mrshoffen.cloudstorage.storage.repository.minio.MinioFolderOperations;
import org.mrshoffen.cloudstorage.storage.repository.minio.MinioOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MinioOperationResolver {

    private final MinioFolderOperations minioFolderService;
    private final MinioFileOperations minioFileService;


    public MinioOperations resolveOperation(String sourcePath) {
        if (isFolderPath(sourcePath)) {
            return minioFolderService;
        } else {
            return minioFileService;
        }
    }


    static boolean isFolderPath(String fullTargetPath) {
        return fullTargetPath.endsWith("/");
    }

}
