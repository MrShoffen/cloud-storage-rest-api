package org.mrshoffen.cloudstorage.storage.exception;

public class StorageObjectNotFoundException extends RuntimeException{

    public StorageObjectNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public StorageObjectNotFoundException(String message) {
        super(message);
    }
}
