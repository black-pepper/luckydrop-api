package com.luckydrop.api.service;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import com.luckydrop.api.security.SecurityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DrawRateLimitServiceTest {

    @Test
    void allowsRequestsWithinLimit() {
        SecurityProperties properties = createSecurityProperties();
        properties.getDrawRateLimit().setIpPerMinute(2);
        DrawRateLimitService service = createService(properties);

        service.checkContentDetail(request("10.0.0.1"), "CONTENT-001");
        service.checkContentDetail(request("10.0.0.1"), "CONTENT-001");
    }

    @Test
    void throwsWhenIpLimitIsExceeded() {
        SecurityProperties properties = createSecurityProperties();
        properties.getDrawRateLimit().setIpPerMinute(1);
        DrawRateLimitService service = createService(properties);

        service.checkContentDetail(request("10.0.0.1"), "CONTENT-001");

        assertRateLimitExceeded(() -> service.checkContentDetail(request("10.0.0.1"), "CONTENT-002"));
    }

    @Test
    void throwsWhenIpContentLimitIsExceeded() {
        SecurityProperties properties = createSecurityProperties();
        properties.getDrawRateLimit().setIpPerMinute(100);
        properties.getDrawRateLimit().setIpContentPerMinute(1);
        DrawRateLimitService service = createService(properties);

        service.checkContentDetail(request("10.0.0.1"), "CONTENT-001");

        assertRateLimitExceeded(() -> service.checkContentDetail(request("10.0.0.1"), "CONTENT-001"));
    }

    @Test
    void throwsWhenIpContentInvitationLimitIsExceeded() {
        SecurityProperties properties = createSecurityProperties();
        properties.getDrawRateLimit().setIpPerMinute(100);
        properties.getDrawRateLimit().setIpContentPerMinute(100);
        properties.getDrawRateLimit().setIpContentInvitationPerMinute(1);
        DrawRateLimitService service = createService(properties);

        service.checkInvitationCodeRequest(request("10.0.0.1"), "CONTENT-001", "INVITE-001");

        assertRateLimitExceeded(
                () -> service.checkInvitationCodeRequest(request("10.0.0.1"), "CONTENT-001", "INVITE-001")
        );
    }

    @Test
    void throwsWhenExecuteIpContentLimitIsExceeded() {
        SecurityProperties properties = createSecurityProperties();
        properties.getDrawRateLimit().setIpPerMinute(100);
        properties.getDrawRateLimit().setIpContentPerMinute(100);
        properties.getDrawRateLimit().setIpContentInvitationPerMinute(100);
        properties.getDrawRateLimit().setExecuteIpContentPerMinute(1);
        DrawRateLimitService service = createService(properties);

        service.checkExecuteRequest(request("10.0.0.1"), "CONTENT-001", "INVITE-001");

        assertRateLimitExceeded(() -> service.checkExecuteRequest(request("10.0.0.1"), "CONTENT-001", "INVITE-002"));
    }

    @Test
    void doesNotConsumeOtherLimitsWhenAnyLimitIsExceeded() {
        SecurityProperties properties = createSecurityProperties();
        properties.getDrawRateLimit().setIpPerMinute(2);
        properties.getDrawRateLimit().setIpContentPerMinute(1);
        DrawRateLimitService service = createService(properties);

        service.checkContentDetail(request("10.0.0.1"), "CONTENT-001");
        assertRateLimitExceeded(() -> service.checkContentDetail(request("10.0.0.1"), "CONTENT-001"));

        service.checkContentDetail(request("10.0.0.1"), "CONTENT-002");
    }

    @Test
    void allowsRequestsAgainAfterWindowRefills() throws InterruptedException {
        SecurityProperties properties = createSecurityProperties();
        properties.getDrawRateLimit().setWindow(Duration.ofMillis(50));
        properties.getDrawRateLimit().setIpPerMinute(1);
        DrawRateLimitService service = createService(properties);

        service.checkContentDetail(request("10.0.0.1"), "CONTENT-001");
        assertRateLimitExceeded(() -> service.checkContentDetail(request("10.0.0.1"), "CONTENT-002"));

        Thread.sleep(80);

        service.checkContentDetail(request("10.0.0.1"), "CONTENT-002");
    }

    @Test
    void usesRemoteAddrByDefault() {
        SecurityProperties properties = createSecurityProperties();
        DrawRateLimitService service = createService(properties);
        MockHttpServletRequest request = request("10.0.0.1");
        request.addHeader("X-Forwarded-For", "203.0.113.10, 10.0.0.2");

        assertThat(service.resolveClientIp(request)).isEqualTo("10.0.0.1");
    }

    @Test
    void usesFirstForwardedIpWhenTrusted() {
        SecurityProperties properties = createSecurityProperties();
        properties.getDrawRateLimit().setTrustXForwardedFor(true);
        DrawRateLimitService service = createService(properties);
        MockHttpServletRequest request = request("10.0.0.1");
        request.addHeader("X-Forwarded-For", "203.0.113.10, 10.0.0.2");

        assertThat(service.resolveClientIp(request)).isEqualTo("203.0.113.10");
    }

    private static void assertRateLimitExceeded(RateLimitedAction action) {
        assertThatThrownBy(action::run)
                .isInstanceOf(DrawEventException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RATE_LIMIT_EXCEEDED);
    }

    private static DrawRateLimitService createService(SecurityProperties properties) {
        return new DrawRateLimitService(properties);
    }

    private static SecurityProperties createSecurityProperties() {
        return new SecurityProperties();
    }

    private static MockHttpServletRequest request(String remoteAddr) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(remoteAddr);
        return request;
    }

    @FunctionalInterface
    private interface RateLimitedAction {
        void run();
    }

}
