package org.mrshoffen.cloudstorage.storage.exception;

public class StorageDownloadException extends RuntimeException{

    public StorageDownloadException(String message, Throwable cause) {
        super(message, cause);
    }

    public StorageDownloadException(Throwable cause) {
        super(cause);
    }
}
