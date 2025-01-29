package org.mrshoffen.cloudstorage.security.v2_rest_controller;


import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
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
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import java.util.Collections;
import java.util.List;


/**
 * В данной реализации используются контроллеры с явным вызовом элементов Spring Security
 * <p>
 * Увидел несколько плюсов в такой реализации
 * 1) Рест контроллеры с аутентификацией более легко читаются, чем спрятанные в кастомных фильтрах,
 * что в итоге придает более REST-ориентированный стиль приложению.
 * 2) Более простой дебаг через Postman
 * 3) Ясность структуры - код процесса аутентификации виден в контроллере, вся логика на поверхности
 * (не нужно сильно копаться в кишках)
 * 4) Нет необходимости в кастомном маппинге входного Json с логином/паролем и в кастомной валидации -
 * всё делается силами Spring
 * <p>
 * Главный минус проистекает из 3го плюса - появляется ручная работа, т.к. код меньше интегрирован в
 * "магию" Spring Security. Приходится вручную управлять процессом аутентификации
 */

@Configuration
@Profile("restControllerSecurity")
@ComponentScan(basePackages = "org.mrshoffen.cloudstorage.security.v2_rest_controller")
public class SecurityRestControllerConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login").permitAll()
                        .anyRequest().authenticated()
                )
                .logout(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    UserDetailsService userDetailsService() {
        User user = new User("alina", "{noop}12345", Collections.emptyList());
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    @Bean
    public List<LogoutHandler> logoutHandlers() {
        return List.of(
                new CookieClearingLogoutHandler("JSESSIONID"),
                new SecurityContextLogoutHandler()
        );
    }
}
