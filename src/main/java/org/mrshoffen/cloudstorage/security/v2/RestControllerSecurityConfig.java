package org.mrshoffen.cloudstorage.security.v2;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.Collections;


/**
 * В данной реализации используются контроллеры с явным вызовом элементов Spring Security
 * <p>
 * Увидел несколько плюсов в такой реализации
 * <p>
 * 1) Рест контроллеры с аутентификацией более легко читаются, чем спрятанные в кастомных фильтрах,
 * что в итоге придает более REST-ориентированный стиль приложению.
 * <p>
 * 2) Более простой дебаг через Postman
 * <p>
 * 4) Нет необходимости в кастомном маппинге входного Json с логином/паролем и в кастомной валидации -
 * всё делается силами Spring
 * <p>
 * Минусы:
 * <p>
 * 1) Главный минус - появляется ручная работа, т.к. код меньше интегрирован в
 * "магию" Spring Security. Приходится вручную управлять процессом аутентификации, что с внедрением
 * Spring Session с Redis так же усложняется.
 *
 * <p>
 * 2) Логика аутентификации слишком "размазывается" между фильтрами и контроллерами
 */

@Configuration
@Profile("restControllerSecurity")
public class RestControllerSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login").permitAll()
                        .anyRequest().authenticated()
                )
                .logout(AbstractHttpConfigurer::disable);

        return httpSecurity.build();
    }

}
