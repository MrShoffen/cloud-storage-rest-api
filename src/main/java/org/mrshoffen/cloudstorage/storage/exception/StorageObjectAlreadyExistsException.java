package org.mrshoffen.cloudstorage.storage.exception;

public class StorageObjectAlreadyExistsException extends RuntimeException{

    public StorageObjectAlreadyExistsException(String message) {
        super(message);
    }
}
