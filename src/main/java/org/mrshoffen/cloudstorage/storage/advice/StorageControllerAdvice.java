package org.mrshoffen.cloudstorage.storage.advice;

import org.mrshoffen.cloudstorage.storage.exception.StorageObjectAlreadyExistsException;
import org.mrshoffen.cloudstorage.storage.exception.StorageObjectNotFoundException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class StorageControllerAdvice {


    @ExceptionHandler(StorageObjectAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleConflictNameException(StorageObjectAlreadyExistsException ex) {
        ProblemDetail problem = generateProblemDetail(CONFLICT, ex.getMessage());
        return ResponseEntity.status(CONFLICT).body(problem);
    }

    @ExceptionHandler(StorageObjectNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleFileNotFoundException(StorageObjectNotFoundException ex) {
        ProblemDetail problem = generateProblemDetail(NOT_FOUND, ex.getMessage());
        return ResponseEntity.status(NOT_FOUND).body(problem);
    }


    private ProblemDetail generateProblemDetail(HttpStatus status, String detail) {
        var problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(status.getReasonPhrase());
        return problemDetail;
    }

}
