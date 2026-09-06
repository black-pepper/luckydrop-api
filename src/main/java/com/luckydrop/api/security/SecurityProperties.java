package com.luckydrop.api.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private final Cors cors = new Cors();
    private final DrawRateLimit drawRateLimit = new DrawRateLimit();
    private final Jwt jwt = new Jwt();
    private final Supabase supabase = new Supabase();
    private List<String> publicPaths = new ArrayList<>();

    @Getter
    @Setter
    public static class Cors {
        private List<String> allowedOrigins = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class DrawRateLimit {
        private boolean enabled = true;
        private boolean trustXForwardedFor = false;
        private Duration window = Duration.ofMinutes(1);
        private long bucketCacheMaxSize = 100_000;
        private int ipPerMinute = 120;
        private int ipContentPerMinute = 30;
        private int ipContentInvitationPerMinute = 10;
        private int executeIpContentPerMinute = 10;
    }

    @Getter
    @Setter
    public static class Jwt {
        private String issuerUri;
        private String jwkSetUri;
        private String audience;
    }

    @Getter
    @Setter
    public static class Supabase {
        private String adminApiUrl;
        private String serviceRoleKey;
    }
}
