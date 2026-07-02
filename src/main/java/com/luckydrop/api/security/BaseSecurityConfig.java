package com.luckydrop.api.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

abstract class BaseSecurityConfig {

    protected void applyCommon(
            HttpSecurity http,
            SecurityProperties securityProperties,
            LoggingAuthenticationEntryPoint authenticationEntryPoint
    ) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource(securityProperties)))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    }

    protected void configurePublicEndpoints(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth,
            SecurityProperties securityProperties
    ) {
        auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
        auth.requestMatchers(securityProperties.getPublicPaths().toArray(String[]::new)).permitAll();
    }

    protected CorsConfigurationSource corsConfigurationSource(SecurityProperties securityProperties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(securityProperties.getCors().getAllowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of(HttpHeaders.RETRY_AFTER));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    protected JwtDecoder jwtDecoder(SecurityProperties securityProperties) {
        String issuerUri = securityProperties.getJwt().getIssuerUri();
        String jwkSetUri = securityProperties.getJwt().getJwkSetUri();

        NimbusJwtDecoder jwtDecoder;
        if (StringUtils.hasText(jwkSetUri)) {
            jwtDecoder = buildJwkSetUriDecoder(jwkSetUri);
        } else {
            jwtDecoder = NimbusJwtDecoder.withIssuerLocation(issuerUri)
                    .jwsAlgorithm(SignatureAlgorithm.ES256)
                    .build();
        }

        OAuth2TokenValidator<Jwt> validator = JwtValidators.createDefaultWithIssuer(issuerUri);
        if (StringUtils.hasText(securityProperties.getJwt().getAudience())) {
            validator = new DelegatingOAuth2TokenValidator<>(
                    validator,
                    new SupabaseAudienceValidator(securityProperties.getJwt().getAudience())
            );
        }
        jwtDecoder.setJwtValidator(validator);
        return jwtDecoder;
    }

    private NimbusJwtDecoder buildJwkSetUriDecoder(String jwkSetUri) {
        try {
            JWKSource<SecurityContext> jwkSource = JWKSourceBuilder
                    .create(new URL(jwkSetUri))
                    .cache(15 * 60 * 1000L, 5 * 60 * 1000L)
                    .retrying(true)
                    .outageTolerant(true)
                    .rateLimited(30 * 1000L)
                    .build();

            DefaultJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
            processor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.ES256, jwkSource));
            return new NimbusJwtDecoder(processor);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid JWK Set URI: " + jwkSetUri, e);
        }
    }
}
