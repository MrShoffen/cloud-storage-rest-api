package org.mrshoffen.cloudstorage.storage.minio;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MinioOperationResolver {

    private final MinioFolderOperations minioFolderService;
    private final MinioFileOperations minioFileService;


    public MinioOperations resolve(String sourcePath) {
        if (isFolderPath(sourcePath)) {
            return minioFolderService;
        } else {
            return minioFileService;
        }
    }

    public MinioOperations any(){
        return minioFileService;
    }

    static boolean isFolderPath(String fullTargetPath) {
        return fullTargetPath.endsWith("/");
    }



}
