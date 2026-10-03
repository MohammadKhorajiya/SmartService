package com.smartservice.security.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CookieUtils {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    public static final String CSRF_TOKEN_COOKIE_NAME = "XSRF-TOKEN";
    public static final String CSRF_HEADER_NAME = "X-CSRF-Token";

    @Value("${app.jwt.refresh-token-expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    @Value("${app.jwt.cookie-secure:false}")
    private boolean cookieSecure;

    @Value("${app.jwt.cookie-same-site:Strict}")
    private String sameSite;

    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(sameSite)
                .path("/api/v1/auth")
                .maxAge(refreshTokenExpirationMs / 1000)
                .build();
    }

    public ResponseCookie createCleanRefreshTokenCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(sameSite)
                .path("/api/v1/auth")
                .maxAge(0)
                .build();
    }

    public ResponseCookie createCsrfTokenCookie(String csrfToken) {
        return ResponseCookie.from(CSRF_TOKEN_COOKIE_NAME, csrfToken)
                .httpOnly(false) // Non-HttpOnly so JavaScript can read document.cookie for double-submit header
                .secure(cookieSecure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(refreshTokenExpirationMs / 1000)
                .build();
    }

    public ResponseCookie createCleanCsrfTokenCookie() {
        return ResponseCookie.from(CSRF_TOKEN_COOKIE_NAME, "")
                .httpOnly(false)
                .secure(cookieSecure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(0)
                .build();
    }

    public String generateCsrfToken() {
        return UUID.randomUUID().toString();
    }
}
