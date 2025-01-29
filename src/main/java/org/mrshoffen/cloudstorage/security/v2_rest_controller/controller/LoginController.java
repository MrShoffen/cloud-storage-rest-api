package org.mrshoffen.cloudstorage.security.v2_rest_controller.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.security.v2_rest_controller.SecurityRestControllerConfig;
import org.mrshoffen.cloudstorage.security.v2_rest_controller.dto.LoginRequest;
import org.mrshoffen.cloudstorage.security.v2_rest_controller.dto.UserResponseDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/auth/login")
@RequiredArgsConstructor
@ConditionalOnBean(SecurityRestControllerConfig.class)
public class LoginController {

    private final AuthenticationManager authenticationManager;

    private final AuthenticationSuccessHandler authenticationSuccessHandler;

    private final AuthenticationFailureHandler authenticationFailureHandler;

    @PostMapping
    public ResponseEntity<UserResponseDto> login(@Valid @RequestBody LoginRequest dto,
                                                 HttpServletRequest request,
                                                 HttpServletResponse response) throws IOException, ServletException {

        var authRequest = new UsernamePasswordAuthenticationToken(dto.username(), dto.password());

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(authRequest);
            authenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);
        } catch (AuthenticationException e) {
            authenticationFailureHandler.onAuthenticationFailure(request, response, e);
            throw e;
        }

        UserResponseDto userResponseDto = new UserResponseDto(authentication.getName());

        return ResponseEntity.ok(userResponseDto);

    }
}
