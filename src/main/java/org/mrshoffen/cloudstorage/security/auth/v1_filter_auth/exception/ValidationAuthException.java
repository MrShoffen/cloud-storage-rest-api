package org.mrshoffen.cloudstorage.security.auth.v1_filter_auth.exception;

import jakarta.validation.ConstraintViolation;
import org.springframework.security.core.AuthenticationException;

import java.util.Set;
import java.util.stream.Collectors;

public class ValidationAuthException extends AuthenticationException {

    public ValidationAuthException(String message) {
        super(message);
    }

    public ValidationAuthException(Set<? extends ConstraintViolation<?>> validationResult) {
        super(validationResult.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("\n"))
        );
    }
}
