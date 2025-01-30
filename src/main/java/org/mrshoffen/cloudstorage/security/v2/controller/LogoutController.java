package org.mrshoffen.cloudstorage.security.v2.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.security.v2.RestControllerSecurityConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/logout") //todo move endpoints to properties?
@RequiredArgsConstructor
@ConditionalOnBean(RestControllerSecurityConfig.class)
public class LogoutController {

//    private final List<LogoutHandler> logoutHandlers;

    @PostMapping
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        session.invalidate();
        return ResponseEntity.noContent().build();
    }
}
