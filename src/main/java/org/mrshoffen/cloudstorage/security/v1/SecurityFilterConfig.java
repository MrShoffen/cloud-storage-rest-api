package org.mrshoffen.cloudstorage.security.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.security.v1.filter.JsonFormAuthenticationFilter;
import org.mrshoffen.cloudstorage.security.v1.handler.LoginFailureHandler;
import org.mrshoffen.cloudstorage.security.v1.handler.LoginSuccessHandler;
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

import java.util.Collections;

@Configuration
@RequiredArgsConstructor
@Profile("filterSecurity")
public class SecurityFilterConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager manager) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable) // Отключаем CSRF для API
                .authorizeHttpRequests(
                        authorization ->
                                authorization.requestMatchers("/auth/login").permitAll()
                                        .anyRequest().authenticated()
                )
                .addFilterBefore(jsonFormAuthFilter(manager), UsernamePasswordAuthenticationFilter.class)
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

    @Bean
    UserDetailsService userDetailsService() {
        User user = new User("alina", "{noop}alina", Collections.emptyList());
        User user2 = new User("anton", "{noop}anton", Collections.emptyList());
        return new InMemoryUserDetailsManager(user, user2);
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public JsonFormAuthenticationFilter jsonFormAuthFilter(AuthenticationManager manager) {
        var filter = new JsonFormAuthenticationFilter("/auth/login", objectMapper);
        filter.setAuthenticationManager(manager);
        filter.setAuthenticationSuccessHandler(new LoginSuccessHandler(objectMapper));
        filter.setAuthenticationFailureHandler(new LoginFailureHandler(objectMapper));
        filter.setSecurityContextRepository(new HttpSessionSecurityContextRepository());
        return filter;
    }
}
