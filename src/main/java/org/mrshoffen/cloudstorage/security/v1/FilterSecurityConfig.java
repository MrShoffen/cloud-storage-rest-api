package org.mrshoffen.cloudstorage.security.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.security.v1.filter.JsonFormAuthenticationFilter;
import org.mrshoffen.cloudstorage.security.v1.handler.LoginFailureHandler;
import org.mrshoffen.cloudstorage.security.v1.handler.LoginSuccessHandler;
import org.mrshoffen.cloudstorage.storage.mapper.StorageUserMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.Collections;


/**
 * В данной реализации используется кастомный Security фильтр на основе AbstractAuthenticationProcessingFilter
 * для обработки Json с логином и паролем.
 * <p>
 * Для аутентификации достаточно соблюдать контракт, который предоставляется базовым фильтром.
 * <p>
 * Для ответов в формате Json - добавлены кастомные хэндлеры для успешной и неудачной аутентификации.
 * <p>
 * Так же в случае кастомного фильтра для интеграции с Spring Session нужно явно указать
 * использовать HttpSessionSecurityContextRepository вместо дефолтного RequestAttributeSecurityContextRepository,
 * который используется в базовом фильтре по умолчанию.
 *
 * <p>
 * Главный плюс - глубокая интеграция в Spring Security, мало ручной работы. Магия происходит сама
 */
@Configuration
@RequiredArgsConstructor
@Profile("filterSecurity")
public class FilterSecurityConfig {

    private final ObjectMapper objectMapper;

    private final Validator validator;

    private final SecurityContextRepository securityContextRepository;

    private final StorageUserMapper storageUserMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager manager) throws Exception {

        var customAuthFilter = new JsonFormAuthenticationFilter("/auth/login",
                objectMapper,
                validator);

        customAuthFilter.setAuthenticationManager(manager);
        customAuthFilter.setAuthenticationSuccessHandler(new LoginSuccessHandler(objectMapper, storageUserMapper));
        customAuthFilter.setAuthenticationFailureHandler(new LoginFailureHandler(objectMapper));
        customAuthFilter.setSecurityContextRepository(securityContextRepository);

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        authorization ->
                                authorization.requestMatchers("/auth/login").permitAll()
                                        .anyRequest().authenticated()
                )
                .addFilterBefore(customAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(
                        logout -> logout
                                .logoutUrl("/auth/logout")
                                .invalidateHttpSession(true)
                                .logoutSuccessHandler(
                                        (_, response, _) ->
                                                response.setStatus(HttpStatus.NO_CONTENT.value())
                                )
                )
                .build();
    }

}
