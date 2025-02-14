package org.mrshoffen.cloudstorage.storage.advice;

import org.mrshoffen.cloudstorage.storage.exception.StorageObjectAlreadyExistsException;
import org.mrshoffen.cloudstorage.storage.exception.StorageObjectNotFoundException;
import org.mrshoffen.cloudstorage.storage.model.dto.response.StorageOperationResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class StorageControllerAdvice {

    @ExceptionHandler(StorageObjectAlreadyExistsException.class)
    public ResponseEntity<StorageOperationResponse> handleConflictNameException(StorageObjectAlreadyExistsException ex) {
        return ResponseEntity.status(CONFLICT)
                .body(
                        StorageOperationResponse.builder()
                                .status(CONFLICT.value())
                                .title(CONFLICT.getReasonPhrase())
                                .detail(ex.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(StorageObjectNotFoundException.class)
    public ResponseEntity<StorageOperationResponse> handleFileNotFoundException(StorageObjectNotFoundException ex) {
        return ResponseEntity.status(NOT_FOUND)
                .body(
                        StorageOperationResponse.builder()
                                .status(NOT_FOUND.value())
                                .title(NOT_FOUND.getReasonPhrase())
                                .detail(ex.getMessage())
                                .build()
                );
    }
}
