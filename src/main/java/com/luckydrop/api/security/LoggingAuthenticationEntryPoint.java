package com.luckydrop.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luckydrop.api.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class LoggingAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final boolean logFailure;

    public LoggingAuthenticationEntryPoint() {
        this(true);
    }

    public LoggingAuthenticationEntryPoint(boolean logFailure) {
        this.logFailure = logFailure;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        if (logFailure) {
            log.warn("Authentication failed. method={}, uri={}",
                    request.getMethod(),
                    request.getRequestURI());
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(ApiResponse.fail("인증 정보가 없습니다.")));
    }
}
