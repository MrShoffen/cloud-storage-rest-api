package org.mrshoffen.cloudstorage.storage.exception;

public class StorageQuotaExceededException extends RuntimeException{
    public StorageQuotaExceededException(String message) {
        super(message);
    }
}
