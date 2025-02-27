package org.mrshoffen.cloudstorage.storage.minio;


import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.storage.minio.operations.MinioFileOperations;
import org.mrshoffen.cloudstorage.storage.minio.operations.MinioFolderOperations;
import org.mrshoffen.cloudstorage.storage.minio.operations.MinioOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MinioOperationResolver {

    private final MinioFolderOperations minioFolderOperations;
    private final MinioFileOperations minioFileOperations;


    public MinioOperations resolve(String sourcePath) {
        if (isFolderPath(sourcePath)) {
            return minioFolderOperations;
        } else {
            return minioFileOperations;
        }
    }

    public MinioOperations any(){
        return minioFileOperations;
    }

    static boolean isFolderPath(String fullTargetPath) {
        return fullTargetPath.endsWith("/");
    }



}
