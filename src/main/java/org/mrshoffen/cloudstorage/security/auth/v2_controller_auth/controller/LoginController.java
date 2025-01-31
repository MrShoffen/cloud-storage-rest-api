package org.mrshoffen.cloudstorage.security.auth.v2_controller_auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.security.dto.LoginRequest;
import org.mrshoffen.cloudstorage.user.dto.StorageUserResponseDto;
import org.mrshoffen.cloudstorage.security.entity.StorageUserDetails;
import org.mrshoffen.cloudstorage.security.auth.v2_controller_auth.RestControllerSecurityConfig;
import org.mrshoffen.cloudstorage.security.service.SecurityContextService;
import org.mrshoffen.cloudstorage.user.entity.StorageUser;
import org.mrshoffen.cloudstorage.user.mapper.StorageUserMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/login")
@RequiredArgsConstructor
@ConditionalOnBean(RestControllerSecurityConfig.class)
public class LoginController {

    private final AuthenticationManager authenticationManager;

    private final SecurityContextService securityContextService;

    private final StorageUserMapper mapper;

    @PostMapping
    public ResponseEntity<StorageUserResponseDto> login(@Valid @RequestBody LoginRequest dto) {
        var authRequest = new UsernamePasswordAuthenticationToken(dto.username(), dto.password());
        Authentication authResult = authenticationManager.authenticate(authRequest);

        securityContextService.saveAuthToContext(authResult);

        StorageUser storageUser = ((StorageUserDetails) authResult.getPrincipal()).getUser();
        return ResponseEntity.ok(mapper.toDto(storageUser));
    }


}
