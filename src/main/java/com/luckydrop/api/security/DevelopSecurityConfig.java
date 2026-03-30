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
    SecurityFilterChain developSecurityFilterChain(HttpSecurity http, SecurityProperties securityProperties) throws Exception {
        applyCommon(http, securityProperties);
        return http
                .authorizeHttpRequests(auth -> {
                    configurePublicEndpoints(auth, securityProperties);
                    auth.anyRequest().authenticated();
                })
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(developJwtDecoder(securityProperties))))
                .build();
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
