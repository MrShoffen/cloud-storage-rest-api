package org.mrshoffen.cloudstorage.security.common;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.Collections;

/**
 * Тут конфигурируется общие для двух конфигураций бины
 *
 */
@Configuration
public class CommonSecurityConfig {


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
    public SecurityContextRepository contextRepository(){
        return new HttpSessionSecurityContextRepository();
    }
}
