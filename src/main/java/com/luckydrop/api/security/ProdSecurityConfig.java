package com.luckydrop.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@Profile("prod")
public class ProdSecurityConfig extends BaseSecurityConfig {

    @Bean
    SecurityFilterChain prodSecurityFilterChain(
            HttpSecurity http,
            SecurityProperties securityProperties,
            SilentAuthenticationEntryPoint silentAuthenticationEntryPoint
    ) throws Exception {
        applyCommon(http, securityProperties, silentAuthenticationEntryPoint);
        return http
                .authorizeHttpRequests(auth -> {
                    configurePublicEndpoints(auth, securityProperties);
                    auth.anyRequest().authenticated();
                })
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(silentAuthenticationEntryPoint)
                        .jwt(jwt -> jwt.decoder(prodJwtDecoder(securityProperties))))
                .build();
    }

    @Bean
    SilentAuthenticationEntryPoint prodAuthenticationEntryPoint() {
        return new SilentAuthenticationEntryPoint();
    }

    @Bean
    JwtDecoder prodJwtDecoder(SecurityProperties securityProperties) {
        return jwtDecoder(securityProperties);
    }

    @Bean
    CorsConfigurationSource prodCorsConfigurationSource(SecurityProperties securityProperties) {
        return corsConfigurationSource(securityProperties);
    }
}
