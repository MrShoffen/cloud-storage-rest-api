package org.mrshoffen.cloudstorage.security.v2_rest_controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mrshoffen.cloudstorage.security.v2_rest_controller.dto.LoginRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;


public class JsonAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final ObjectMapper objectMapper;

    protected JsonAuthenticationFilter(String defaultFilterProcessesUrl,
                                       ObjectMapper objectMapper,
                                       AuthenticationSuccessHandler successHandler,
                                       AuthenticationFailureHandler failureHandler) {
        super(defaultFilterProcessesUrl);
        this.objectMapper = objectMapper;
        setAuthenticationSuccessHandler(successHandler);
        setAuthenticationFailureHandler(failureHandler);
    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // Проверяем, что запрос содержит JSON
        if (!request.getContentType().equals("application/json")) {
            throw new IllegalArgumentException("Content type must be application/json");
        }

        // Читаем тело запроса как объект LoginRequest
        LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);

        // Создаем объект Authentication (UsernamePasswordAuthenticationToken)
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password());

        // Передаем объект Authentication в AuthenticationManager для проверки
        return this.getAuthenticationManager().authenticate(authenticationToken);
    }

}