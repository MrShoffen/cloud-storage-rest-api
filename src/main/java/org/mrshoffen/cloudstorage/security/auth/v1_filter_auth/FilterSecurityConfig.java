package org.mrshoffen.cloudstorage.security.auth.v1_filter_auth;

import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.security.auth.v1_filter_auth.filter.JsonFormAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


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

    private final JsonFormAuthenticationFilter jsonFormAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager manager) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        authorization ->
                                authorization
                                        .requestMatchers("/auth/login", "/api/v1/users").permitAll()
                                        .anyRequest().authenticated()
                )
                .addFilterBefore(jsonFormAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
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
