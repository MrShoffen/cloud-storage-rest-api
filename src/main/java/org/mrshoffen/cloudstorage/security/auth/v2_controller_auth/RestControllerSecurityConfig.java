package org.mrshoffen.cloudstorage.security.auth.v2_controller_auth;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;


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
@RequiredArgsConstructor
public class RestControllerSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login", "/api/v1/users").permitAll()
                        .anyRequest().authenticated()
                )
                .logout(AbstractHttpConfigurer::disable);

        return httpSecurity.build();
    }

}
