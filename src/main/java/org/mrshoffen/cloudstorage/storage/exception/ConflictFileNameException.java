package org.mrshoffen.cloudstorage.storage.exception;

public class ConflictFileNameException extends RuntimeException{

    public ConflictFileNameException(String message) {
        super(message);
    }
}
