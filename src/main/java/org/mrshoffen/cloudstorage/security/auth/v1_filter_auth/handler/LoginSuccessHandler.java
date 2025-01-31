package org.mrshoffen.cloudstorage.security.auth.v1_filter_auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.security.entity.StorageUserDetails;
import org.mrshoffen.cloudstorage.security.auth.v1_filter_auth.FilterSecurityConfig;
import org.mrshoffen.cloudstorage.user.entity.User;
import org.mrshoffen.cloudstorage.user.mapper.UserMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RequiredArgsConstructor
@Component
@ConditionalOnBean(FilterSecurityConfig.class)
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;

    private final UserMapper mapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        User user = ((StorageUserDetails) authentication.getPrincipal()).getUser();

        objectMapper.writeValue(
                response.getWriter(),
                mapper.toDto(user)
        );
    }
}