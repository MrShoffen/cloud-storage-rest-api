package org.mrshoffen.cloudstorage.security.auth.v2_controller_auth.advice;

import org.mrshoffen.cloudstorage.security.auth.v2_controller_auth.RestControllerSecurityConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@ConditionalOnBean(RestControllerSecurityConfig.class)
public class SecurityControllerAdvice {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCredentialsException(BadCredentialsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password"
        );
        problem.setTitle("Unauthorized");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

}
