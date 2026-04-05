package com.luckydrop.api.security;

public class SilentAuthenticationEntryPoint extends LoggingAuthenticationEntryPoint {

    public SilentAuthenticationEntryPoint() {
        super(false);
    }
}
