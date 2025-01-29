package org.mrshoffen.cloudstorage.security.v2_rest_controller.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.security.v2_rest_controller.SecurityRestControllerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RequiredArgsConstructor
@Component
@ConditionalOnBean(SecurityRestControllerConfig.class)
public class ClearContextAuthFailureHandler implements AuthenticationFailureHandler {

    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();


    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
            this.securityContextHolderStrategy.clearContext();
    }
}
