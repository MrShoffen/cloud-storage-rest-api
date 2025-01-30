package org.mrshoffen.cloudstorage.security.auth.v1_filter_auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.mrshoffen.cloudstorage.security.auth.v1_filter_auth.FilterSecurityConfig;
import org.mrshoffen.cloudstorage.security.auth.v1_filter_auth.exception.ValidationAuthException;
import org.mrshoffen.cloudstorage.security.dto.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;


@Component
@ConditionalOnBean(FilterSecurityConfig.class)
public class JsonFormAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private final ObjectMapper objectMapper;

    private final Validator validator;

    @Autowired
    public JsonFormAuthenticationFilter(@Value("/auth/login") String defaultFilterProcessesUrl,
                                        AuthenticationManager authenticationManager,
                                        ObjectMapper objectMapper,
                                        Validator validator) {

        super(defaultFilterProcessesUrl, authenticationManager);
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @Autowired
    public void setHandlersAndRepository(
            AuthenticationSuccessHandler loginSuccessHandler,
            AuthenticationFailureHandler loginFailureHandler,
            SecurityContextRepository securityContextRepository) {

        setAuthenticationSuccessHandler(loginSuccessHandler);
        setAuthenticationFailureHandler(loginFailureHandler);
        setSecurityContextRepository(securityContextRepository);
    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response
    ) throws AuthenticationException, IOException {

        if ( !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        LoginRequest loginRequest = objectMapper.readValue(
                request.getInputStream(),
                LoginRequest.class
        );

        Set<ConstraintViolation<LoginRequest>> validationResult = validator.validate(loginRequest);
        if (!validationResult.isEmpty()) {
            throw new ValidationAuthException(validationResult);
        }

        var authRequest = new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password());
        return getAuthenticationManager().authenticate(authRequest);
    }

}