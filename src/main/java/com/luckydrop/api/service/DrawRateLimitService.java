package com.luckydrop.api.service;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import com.luckydrop.api.security.SecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class DrawRateLimitService {

    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final String UNKNOWN_IP = "unknown";
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    private final SecurityProperties.DrawRateLimit properties;
    private final Clock clock;
    private final ConcurrentHashMap<RateLimitKey, WindowCounter> counters = new ConcurrentHashMap<>();

    @Autowired
    public DrawRateLimitService(SecurityProperties securityProperties) {
        this(securityProperties, Clock.systemUTC());
    }

    DrawRateLimitService(SecurityProperties securityProperties, Clock clock) {
        this.properties = securityProperties.getDrawRateLimit();
        this.clock = clock;
    }

    public void checkContentDetail(HttpServletRequest request, String contentCode) {
        if (!properties.isEnabled()) {
            return;
        }

        String clientIp = resolveClientIp(request);
        consume(RateLimitType.IP, clientIp, null, null, properties.getIpPerMinute());
        consume(RateLimitType.IP_CONTENT, clientIp, contentCode, null, properties.getIpContentPerMinute());
    }

    public void checkInvitationCodeRequest(HttpServletRequest request, String contentCode, String invitationCode) {
        if (!properties.isEnabled()) {
            return;
        }

        String clientIp = resolveClientIp(request);
        consume(RateLimitType.IP, clientIp, null, null, properties.getIpPerMinute());
        consume(RateLimitType.IP_CONTENT, clientIp, contentCode, null, properties.getIpContentPerMinute());
        consume(
                RateLimitType.IP_CONTENT_INVITATION,
                clientIp,
                contentCode,
                invitationCode,
                properties.getIpContentInvitationPerMinute()
        );
    }

    public void checkExecuteRequest(HttpServletRequest request, String contentCode, String invitationCode) {
        if (!properties.isEnabled()) {
            return;
        }

        String clientIp = resolveClientIp(request);
        consume(RateLimitType.IP, clientIp, null, null, properties.getIpPerMinute());
        consume(RateLimitType.IP_CONTENT, clientIp, contentCode, null, properties.getIpContentPerMinute());
        consume(
                RateLimitType.IP_CONTENT_INVITATION,
                clientIp,
                contentCode,
                invitationCode,
                properties.getIpContentInvitationPerMinute()
        );
        consume(
                RateLimitType.EXECUTE_IP_CONTENT,
                clientIp,
                contentCode,
                null,
                properties.getExecuteIpContentPerMinute()
        );
    }

    String resolveClientIp(HttpServletRequest request) {
        if (properties.isTrustXForwardedFor()) {
            String forwardedFor = request.getHeader(X_FORWARDED_FOR);
            if (StringUtils.hasText(forwardedFor)) {
                return forwardedFor.split(",", 2)[0].trim();
            }
        }

        String remoteAddr = request.getRemoteAddr();
        return StringUtils.hasText(remoteAddr) ? remoteAddr : UNKNOWN_IP;
    }

    private void consume(
            RateLimitType type,
            String clientIp,
            String contentCode,
            String invitationCode,
            int limit
    ) {
        if (limit <= 0) {
            return;
        }

        long nowMillis = clock.millis();
        long windowStartMillis = currentWindowStartMillis(nowMillis);
        cleanupExpired(windowStartMillis);

        RateLimitKey key = new RateLimitKey(type, clientIp, contentCode, invitationCode);
        AtomicBoolean exceeded = new AtomicBoolean(false);

        counters.compute(key, (ignored, counter) -> {
            if (counter == null || counter.isBefore(windowStartMillis)) {
                return new WindowCounter(windowStartMillis, 1);
            }
            if (counter.count() >= limit) {
                exceeded.set(true);
                return counter;
            }
            return counter.increment();
        });

        if (exceeded.get()) {
            throw new DrawEventException(ErrorCode.RATE_LIMIT_EXCEEDED);
        }
    }

    private long currentWindowStartMillis(long nowMillis) {
        long windowMillis = WINDOW.toMillis();
        return nowMillis - (nowMillis % windowMillis);
    }

    private void cleanupExpired(long currentWindowStartMillis) {
        Iterator<Map.Entry<RateLimitKey, WindowCounter>> iterator = counters.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<RateLimitKey, WindowCounter> entry = iterator.next();
            if (entry.getValue().isBefore(currentWindowStartMillis)) {
                iterator.remove();
            }
        }
    }

    private enum RateLimitType {
        IP,
        IP_CONTENT,
        IP_CONTENT_INVITATION,
        EXECUTE_IP_CONTENT
    }

    private record RateLimitKey(
            RateLimitType type,
            String clientIp,
            String contentCode,
            String invitationCode
    ) {
        private RateLimitKey {
            clientIp = Objects.toString(clientIp, "");
            contentCode = Objects.toString(contentCode, "");
            invitationCode = Objects.toString(invitationCode, "");
        }
    }

    private record WindowCounter(long windowStartMillis, int count) {
        private boolean isBefore(long currentWindowStartMillis) {
            return windowStartMillis < currentWindowStartMillis;
        }

        private WindowCounter increment() {
            return new WindowCounter(windowStartMillis, count + 1);
        }
    }
}
