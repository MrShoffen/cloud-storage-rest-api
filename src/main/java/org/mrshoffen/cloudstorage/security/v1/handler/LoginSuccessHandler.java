package org.mrshoffen.cloudstorage.security.v1.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.security.common.dto.StorageUserResponseDto;
import org.mrshoffen.cloudstorage.security.common.entity.StorageUserDetails;
import org.mrshoffen.cloudstorage.storage.entity.StorageUser;
import org.mrshoffen.cloudstorage.storage.mapper.StorageUserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;

    private final StorageUserMapper mapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        StorageUser storageUser = ((StorageUserDetails) authentication.getPrincipal()).getUser();

        objectMapper.writeValue(
                response.getWriter(),
                mapper.toDto(storageUser)
        );
    }
}