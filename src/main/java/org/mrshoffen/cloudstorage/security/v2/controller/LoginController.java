package org.mrshoffen.cloudstorage.security.v2.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.security.common.dto.LoginRequest;
import org.mrshoffen.cloudstorage.security.common.dto.StorageUserResponseDto;
import org.mrshoffen.cloudstorage.security.common.entity.StorageUserDetails;
import org.mrshoffen.cloudstorage.security.v2.RestControllerSecurityConfig;
import org.mrshoffen.cloudstorage.storage.entity.StorageUser;
import org.mrshoffen.cloudstorage.storage.mapper.StorageUserMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/login")
@RequiredArgsConstructor
@ConditionalOnBean(RestControllerSecurityConfig.class)
public class LoginController {

    private final AuthenticationManager authenticationManager;

    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

    private final SecurityContextRepository contextRepository;

    private final StorageUserMapper mapper;

    @PostMapping
    public ResponseEntity<StorageUserResponseDto> login(@Valid @RequestBody LoginRequest dto,
                                                        HttpServletRequest request, HttpServletResponse response) {

        var authRequest = new UsernamePasswordAuthenticationToken(dto.username(), dto.password());
        Authentication authResult = authenticationManager.authenticate(authRequest);
        saveSecurityContext(request, response, authResult);

        StorageUser storageUser = ((StorageUserDetails) authResult.getPrincipal()).getUser();


        return ResponseEntity.ok(mapper.toDto(storageUser));
    }

    private void saveSecurityContext(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);

        contextRepository.saveContext(context, request, response);
    }
}
