package org.mrshoffen.cloudstorage.security.auth.v1_filter_auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.security.auth.v1_filter_auth.FilterSecurityConfig;
import org.mrshoffen.cloudstorage.security.auth.v1_filter_auth.exception.ValidationAuthException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static org.springframework.http.HttpStatus.*;

@RequiredArgsConstructor
@Component
@ConditionalOnBean(FilterSecurityConfig.class)
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException exception
    ) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ProblemDetail problemDetail = specifyExceptionType(exception);
        response.setStatus(problemDetail.getStatus());

        objectMapper.writeValue(
                response.getWriter(),
                problemDetail
        );
    }

    private ProblemDetail specifyExceptionType(AuthenticationException exception) {
        return switch (exception) {
            case BadCredentialsException unAuth -> generateProblemDetail(UNAUTHORIZED,"Неправильное имя пользователя или пароль");
            case ValidationAuthException badReq -> generateProblemDetail(BAD_REQUEST, exception.getMessage());
            case AuthenticationServiceException notAll -> generateProblemDetail(METHOD_NOT_ALLOWED, exception.getMessage());
            default -> generateProblemDetail(INTERNAL_SERVER_ERROR, exception.getMessage());
        };
    }

    private ProblemDetail generateProblemDetail(HttpStatus status, String detail) {
        var problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(status.getReasonPhrase());
        return problemDetail;
    }

}