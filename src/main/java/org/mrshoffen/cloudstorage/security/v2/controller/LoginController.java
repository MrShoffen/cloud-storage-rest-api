package org.mrshoffen.cloudstorage.security.v2.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.security.v2.SecurityRestControllerConfig;
import org.mrshoffen.cloudstorage.security.common.dto.LoginRequest;
import org.mrshoffen.cloudstorage.security.common.dto.UserResponseDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/login")
@RequiredArgsConstructor
@ConditionalOnBean(SecurityRestControllerConfig.class)
public class LoginController {

    private final AuthenticationManager authenticationManager;

    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

    private static final String SPRING_SECURITY_CONTEXT_ATTRIBUTE = "SPRING_SECURITY_CONTEXT";


    @PostMapping
    public ResponseEntity<UserResponseDto> login(@Valid @RequestBody LoginRequest dto,
                                                 HttpServletRequest request) {

        var authRequest = new UsernamePasswordAuthenticationToken(dto.username(), dto.password());
        Authentication authentication = authenticationManager.authenticate(authRequest);
        saveSecurityContext(request, authentication);

        UserResponseDto userResponseDto = new UserResponseDto(authentication.getName());
        return ResponseEntity.ok(userResponseDto);
    }

    private void saveSecurityContext(HttpServletRequest request, Authentication authentication) {
        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);

        HttpSession session = request.getSession();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
    }


    @GetMapping("/test")
    public ResponseEntity<String> sessionTest(HttpServletRequest request, @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(user.getUsername());
    }
}
