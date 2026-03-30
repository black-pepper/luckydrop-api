package com.luckydrop.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@Profile("local")
public class LocalSecurityConfig extends BaseSecurityConfig {

    @Bean
    SecurityFilterChain localSecurityFilterChain(HttpSecurity http, SecurityProperties securityProperties) throws Exception {
        applyCommon(http, securityProperties);
        return http
                .authorizeHttpRequests(auth -> {
                    configurePublicEndpoints(auth, securityProperties);
                    auth.anyRequest().permitAll();
                })
                .build();
    }

    @Bean
    CorsConfigurationSource localCorsConfigurationSource(SecurityProperties securityProperties) {
        return corsConfigurationSource(securityProperties);
    }
}
