package org.mrshoffen.cloudstorage.security.v2_rest_controller.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.security.v2_rest_controller.SecurityRestControllerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth/logout") //todo move endpoints to properties?
@RequiredArgsConstructor
@ConditionalOnBean(SecurityRestControllerConfig.class)
public class LogoutController {

    private final List<LogoutHandler> logoutHandlers;

    @PostMapping
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        logoutHandlers.forEach(handler -> handler.logout(request, response, authentication));

        return ResponseEntity.noContent().build();
    }
}
