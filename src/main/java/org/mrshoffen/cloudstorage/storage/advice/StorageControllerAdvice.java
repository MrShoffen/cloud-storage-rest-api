package org.mrshoffen.cloudstorage.storage.advice;

import org.mrshoffen.cloudstorage.storage.exception.ConflictNameException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class StorageControllerAdvice {


    @ExceptionHandler(ConflictNameException.class)
    public ResponseEntity<ProblemDetail> handleConflictNameException(ConflictNameException ex) {
        ProblemDetail problem = generateProblemDetail(CONFLICT, ex.getMessage());
        return ResponseEntity.status(CONFLICT).body(problem);
    }


    private ProblemDetail generateProblemDetail(HttpStatus status, String detail) {
        var problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(status.getReasonPhrase());
        return problemDetail;
    }

}
