package com.luckydrop.api.security;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Component
public class CurrentUserAuthIdProvider {

    public UUID getCurrentAuthId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new DrawEventException(ErrorCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Jwt jwt)) {
            throw new DrawEventException(ErrorCode.UNAUTHORIZED);
        }

        String subject = jwt.getSubject();
        if (!StringUtils.hasText(subject)) {
            throw new DrawEventException(ErrorCode.UNAUTHORIZED);
        }

        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException e) {
            throw new DrawEventException(ErrorCode.UNAUTHORIZED);
        }
    }
}
