package org.mrshoffen.cloudstorage.storage.exception;

public class UserStorageCapacityExceeded extends RuntimeException{
    public UserStorageCapacityExceeded(String message) {
        super(message);
    }
}
