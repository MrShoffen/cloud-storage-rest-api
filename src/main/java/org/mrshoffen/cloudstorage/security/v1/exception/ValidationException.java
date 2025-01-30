package org.mrshoffen.cloudstorage.security.v1.exception;

import jakarta.validation.ConstraintViolation;
import org.mrshoffen.cloudstorage.security.common.dto.LoginRequest;
import org.springframework.security.core.AuthenticationException;

import java.util.Set;
import java.util.stream.Collectors;

public class ValidationException extends AuthenticationException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(Set<? extends ConstraintViolation<?>> validationResult) {
        super(validationResult.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("\n"))
        );
    }
}
