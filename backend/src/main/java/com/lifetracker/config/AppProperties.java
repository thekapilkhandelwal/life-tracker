package com.lifetracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String frontendUrl,
        String publicBaseUrl,
        Jwt jwt,
        Cookie cookie,
        String mailFrom,
        boolean demoLoginEnabled,
        boolean googleOAuthEnabled
) {
    public record Jwt(String secret, String cookieName, long expirationDays) {
    }

    public record Cookie(boolean secure, String sameSite) {
    }
}
