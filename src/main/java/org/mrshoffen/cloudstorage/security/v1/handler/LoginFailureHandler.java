package org.mrshoffen.cloudstorage.security.v1.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.mrshoffen.cloudstorage.security.v1.FilterSecurityConfig;
import org.mrshoffen.cloudstorage.security.v1.exception.ValidationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RequiredArgsConstructor
@Component
@ConditionalOnBean(FilterSecurityConfig.class)
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException exception
    ) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ProblemDetail problemDetail = null;


        if (exception instanceof BadCredentialsException) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            problemDetail = ProblemDetail.forStatusAndDetail(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid username or password"
            );
            problemDetail.setTitle("Unauthorized");
        }

        if(exception instanceof ValidationException){
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            problemDetail = ProblemDetail.forStatusAndDetail(
                    HttpStatus.BAD_REQUEST,
                    exception.getMessage()
            );
            problemDetail.setTitle("Bad Request");
        }


        objectMapper.writeValue(
                response.getWriter(),
                problemDetail
        );
    }
}