package com.luckydrop.api.security;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.UUID;

@Component
public class CurrentUserAuthIdProvider {

    public UUID getCurrentAuthId() {
        String subject = getCurrentJwt().getSubject();
        if (!StringUtils.hasText(subject)) {
            throw new DrawEventException(ErrorCode.UNAUTHORIZED);
        }
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException e) {
            throw new DrawEventException(ErrorCode.UNAUTHORIZED);
        }
    }

    public String getCurrentUserDisplayName() {
        Jwt jwt = getCurrentJwt();
        Map<String, Object> userMetadata = jwt.getClaim("user_metadata");
        if (userMetadata != null) {
            String fullName = getStringClaim(userMetadata, "full_name");
            if (fullName != null) return fullName;
            String name = getStringClaim(userMetadata, "name");
            if (name != null) return name;
        }
        String email = jwt.getClaim("email");
        return StringUtils.hasText(email) ? email : null;
    }

    private Jwt getCurrentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new DrawEventException(ErrorCode.UNAUTHORIZED);
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Jwt jwt)) {
            throw new DrawEventException(ErrorCode.UNAUTHORIZED);
        }
        return jwt;
    }

    private String getStringClaim(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return (value instanceof String s && StringUtils.hasText(s)) ? s : null;
    }
}
