package org.mrshoffen.cloudstorage.security.v1.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.SneakyThrows;
import org.mrshoffen.cloudstorage.security.common.dto.LoginRequest;
import org.mrshoffen.cloudstorage.security.v1.FilterSecurityConfig;
import org.mrshoffen.cloudstorage.security.v1.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
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
        LoginRequest loginRequest = objectMapper.readValue(
                request.getInputStream(),
                LoginRequest.class
        );

        Set<ConstraintViolation<LoginRequest>> validationResult = validator.validate(loginRequest);
        if (!validationResult.isEmpty()) {
            throw new ValidationException(validationResult);
        }

        var authRequest = new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password());
        return getAuthenticationManager().authenticate(authRequest);
    }

}