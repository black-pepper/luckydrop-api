package com.luckydrop.api.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import com.luckydrop.api.security.SecurityProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import io.github.bucket4j.EstimationProbe;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class DrawRateLimitService {

    private static final Duration DEFAULT_WINDOW = Duration.ofMinutes(1);
    private static final String UNKNOWN_IP = "unknown";
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    private final SecurityProperties.DrawRateLimit properties;
    private final Cache<RateLimitKey, Bucket> buckets;
    private final Cache<String, Object> consumeLocks;

    @Autowired
    public DrawRateLimitService(SecurityProperties securityProperties) {
        this.properties = securityProperties.getDrawRateLimit();
        this.buckets = Caffeine.newBuilder()
                .maximumSize(this.properties.getBucketCacheMaxSize())
                .expireAfterAccess(bucketTtl())
                .build();
        this.consumeLocks = Caffeine.newBuilder()
                .maximumSize(this.properties.getBucketCacheMaxSize())
                .expireAfterAccess(bucketTtl())
                .build();
    }

    public void checkContentDetail(HttpServletRequest request, String contentCode) {
        if (!properties.isEnabled()) {
            return;
        }

        String clientIp = resolveClientIp(request);
        consumeAll(clientIp, List.of(
                new RateLimitRule(RateLimitType.IP, clientIp, null, null, properties.getIpPerMinute()),
                new RateLimitRule(RateLimitType.IP_CONTENT, clientIp, contentCode, null, properties.getIpContentPerMinute())
        ));
    }

    public void checkInvitationCodeRequest(HttpServletRequest request, String contentCode, String invitationCode) {
        if (!properties.isEnabled()) {
            return;
        }

        String clientIp = resolveClientIp(request);
        consumeAll(clientIp, List.of(
                new RateLimitRule(RateLimitType.IP, clientIp, null, null, properties.getIpPerMinute()),
                new RateLimitRule(RateLimitType.IP_CONTENT, clientIp, contentCode, null, properties.getIpContentPerMinute()),
                new RateLimitRule(
                        RateLimitType.IP_CONTENT_INVITATION,
                        clientIp,
                        contentCode,
                        invitationCode,
                        properties.getIpContentInvitationPerMinute()
                )
        ));
    }

    public void checkExecuteRequest(HttpServletRequest request, String contentCode, String invitationCode) {
        if (!properties.isEnabled()) {
            return;
        }

        String clientIp = resolveClientIp(request);
        consumeAll(clientIp, List.of(
                new RateLimitRule(RateLimitType.IP, clientIp, null, null, properties.getIpPerMinute()),
                new RateLimitRule(RateLimitType.IP_CONTENT, clientIp, contentCode, null, properties.getIpContentPerMinute()),
                new RateLimitRule(
                        RateLimitType.IP_CONTENT_INVITATION,
                        clientIp,
                        contentCode,
                        invitationCode,
                        properties.getIpContentInvitationPerMinute()
                ),
                new RateLimitRule(
                        RateLimitType.EXECUTE_IP_CONTENT,
                        clientIp,
                        contentCode,
                        null,
                        properties.getExecuteIpContentPerMinute()
                )
        ));
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

    private void consumeAll(String clientIp, List<RateLimitRule> rules) {
        List<RateLimitRule> activeRules = rules.stream()
                .filter(RateLimitRule::isEnabled)
                .toList();
        if (activeRules.isEmpty()) {
            return;
        }

        Object consumeLock = consumeLocks.get(clientIp, ignored -> new Object());
        synchronized (consumeLock) {
            List<Bucket> activeBuckets = activeRules.stream()
                    .map(rule -> buckets.get(rule.key(), ignored -> newBucket(rule.limit())))
                    .toList();
            long nanosToWaitForRefill = activeBuckets.stream()
                    .map(bucket -> bucket.estimateAbilityToConsume(1))
                    .filter(probe -> !probe.canBeConsumed())
                    .mapToLong(EstimationProbe::getNanosToWaitForRefill)
                    .max()
                    .orElse(0L);
            if (nanosToWaitForRefill > 0) {
                throw rateLimitExceeded(nanosToWaitForRefill);
            }
            boolean consumed = activeBuckets.stream().allMatch(bucket -> bucket.tryConsume(1));
            if (!consumed) {
                long retryAfterNanos = activeBuckets.stream()
                        .map(bucket -> bucket.estimateAbilityToConsume(1))
                        .filter(probe -> !probe.canBeConsumed())
                        .mapToLong(EstimationProbe::getNanosToWaitForRefill)
                        .max()
                        .orElse(window().toNanos());
                throw rateLimitExceeded(retryAfterNanos);
            }
        }
    }

    private DrawEventException rateLimitExceeded(long nanosToWaitForRefill) {
        long retryAfterSeconds = Math.max(1L, TimeUnit.NANOSECONDS.toSeconds(nanosToWaitForRefill - 1) + 1);
        return new DrawEventException(ErrorCode.RATE_LIMIT_EXCEEDED, retryAfterSeconds);
    }

    private Bucket newBucket(int limit) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(limit)
                        .refillGreedy(limit, window())
                        .build())
                .build();
    }

    private Duration window() {
        Duration window = properties.getWindow();
        if (window == null || window.isZero() || window.isNegative()) {
            return DEFAULT_WINDOW;
        }
        return window;
    }

    private Duration bucketTtl() {
        return window().multipliedBy(2);
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

    private record RateLimitRule(
            RateLimitType type,
            String clientIp,
            String contentCode,
            String invitationCode,
            int limit
    ) {
        private boolean isEnabled() {
            return limit > 0;
        }

        private RateLimitKey key() {
            return new RateLimitKey(type, clientIp, contentCode, invitationCode);
        }
    }
}
