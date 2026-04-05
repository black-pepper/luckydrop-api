package com.luckydrop.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@Profile("develop")
public class DevelopSecurityConfig extends BaseSecurityConfig {

    @Bean
    SecurityFilterChain developSecurityFilterChain(
            HttpSecurity http,
            SecurityProperties securityProperties,
            LoggingAuthenticationEntryPoint loggingAuthenticationEntryPoint
    ) throws Exception {
        applyCommon(http, securityProperties, loggingAuthenticationEntryPoint);
        return http
                .authorizeHttpRequests(auth -> {
                    configurePublicEndpoints(auth, securityProperties);
                    auth.anyRequest().authenticated();
                })
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(loggingAuthenticationEntryPoint)
                        .jwt(jwt -> jwt.decoder(developJwtDecoder(securityProperties))))
                .build();
    }

    @Bean
    LoggingAuthenticationEntryPoint developAuthenticationEntryPoint() {
        return new LoggingAuthenticationEntryPoint();
    }

    @Bean
    JwtDecoder developJwtDecoder(SecurityProperties securityProperties) {
        return jwtDecoder(securityProperties);
    }

    @Bean
    CorsConfigurationSource developCorsConfigurationSource(SecurityProperties securityProperties) {
        return corsConfigurationSource(securityProperties);
    }
}
