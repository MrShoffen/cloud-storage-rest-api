package org.mrshoffen.cloudstorage.storage.exception;

public class MinioOperationException extends RuntimeException{

    public MinioOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
