package org.mrshoffen.cloudstorage.user.http.advice;

import org.mrshoffen.cloudstorage.user.exception.IncorrectPasswordException;
import org.mrshoffen.cloudstorage.user.exception.UserAlreadyExistsException;
import org.mrshoffen.cloudstorage.user.exception.UserNotFoundException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class UserControllerAdvice extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String errors = e.getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining(" | "));
        var problemDetail = ProblemDetail.forStatusAndDetail(BAD_REQUEST, errors);
        problemDetail.setTitle("Bad Request");
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(problemDetail);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleUserAlreadyExistException(UserAlreadyExistsException e) {
        ProblemDetail problem = generateProblemDetail(CONFLICT, e.getMessage());
        return ResponseEntity.status(UNAUTHORIZED).body(problem);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleUserNotFoundException(UserNotFoundException e) {
        ProblemDetail problem = generateProblemDetail(NOT_FOUND, e.getMessage());
        return ResponseEntity.status(NOT_FOUND).body(problem);
    }

    @ExceptionHandler(IncorrectPasswordException.class)
    public ResponseEntity<ProblemDetail> handleIncorrectPasswordException(IncorrectPasswordException e) {
        ProblemDetail problem = generateProblemDetail(UNAUTHORIZED, e.getMessage());
        return ResponseEntity.status(UNAUTHORIZED).body(problem);
    }

    private ProblemDetail generateProblemDetail(HttpStatus status, String detail) {
        var problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(status.getReasonPhrase());
        return problemDetail;
    }

}
