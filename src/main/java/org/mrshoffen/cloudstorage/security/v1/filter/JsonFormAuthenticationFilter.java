package org.mrshoffen.cloudstorage.security.v1.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.SneakyThrows;
import org.mrshoffen.cloudstorage.security.common.dto.LoginRequest;
import org.mrshoffen.cloudstorage.security.v1.exception.ValidationException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import java.io.IOException;
import java.util.Set;


public class JsonFormAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private final ObjectMapper objectMapper;

    private final Validator validator;

    public JsonFormAuthenticationFilter(String defaultFilterProcessesUrl, ObjectMapper objectMapper, Validator validator) {
        super(defaultFilterProcessesUrl);
        this.objectMapper = objectMapper;
        this.validator = validator;
    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response
    ) throws AuthenticationException, IOException {
        LoginRequest loginRequest = objectMapper.readValue(
                request.getInputStream(),
                LoginRequest.class
        );

        Set<ConstraintViolation<LoginRequest>> validationResult = validator.validate(loginRequest);
        if (!validationResult.isEmpty()) {
            throw new ValidationException(validationResult);
        }

        var authRequest = new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password());
        return getAuthenticationManager().authenticate(authRequest);
    }

}