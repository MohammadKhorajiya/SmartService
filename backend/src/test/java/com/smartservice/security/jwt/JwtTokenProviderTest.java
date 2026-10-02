package com.smartservice.security.jwt;

import com.smartservice.domain.user.Role;
import com.smartservice.security.userDetails.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider createProvider(String secret, long expirationMs) {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", secret);
        ReflectionTestUtils.setField(provider, "jwtExpirationMs", expirationMs);
        return provider;
    }

    @Test
    @DisplayName("1. Valid production-style raw secret with hyphens generates valid access token")
    void testProductionRawSecretWithHyphens() {
        String prodSecret = "smartservice-production-super-secret-jwt-key-2026-very-secure-phrase";
        JwtTokenProvider provider = createProvider(prodSecret, 3600000);

        String token = provider.generateAccessTokenFromEmail("admin@smartservice.com", 1L, "ADMIN");
        assertNotNull(token);
        assertTrue(provider.validateToken(token));
        assertEquals("admin@smartservice.com", provider.getEmailFromToken(token));
    }

    @Test
    @DisplayName("2. Base64-encoded secret creates signing key and token successfully")
    void testBase64SecretSuccess() {
        String base64Secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
        JwtTokenProvider provider = createProvider(base64Secret, 3600000);

        String token = provider.generateAccessTokenFromEmail("manager@smartservice.com", 2L, "MANAGER");
        assertNotNull(token);
        assertTrue(provider.validateToken(token));
        assertEquals("manager@smartservice.com", provider.getEmailFromToken(token));
    }

    @Test
    @DisplayName("3. Access token generation with Authentication object succeeds")
    void testGenerateAccessTokenFromAuthentication() {
        String secret = "another-production-style-jwt-secret-string-for-smartservice-platform-2026";
        JwtTokenProvider provider = createProvider(secret, 3600000);

        CustomUserDetails userDetails = new CustomUserDetails(
                5L, "tech.alex@smartservice.com", "Password@123", "Alex Tech", Role.TECHNICIAN, null, java.util.Collections.emptyList()
        );
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        String token = provider.generateAccessToken(auth);
        assertNotNull(token);
        assertTrue(provider.validateToken(token));
        assertEquals("tech.alex@smartservice.com", provider.getEmailFromToken(token));
    }

    @Test
    @DisplayName("4. Token validation returns false for tampered or invalid tokens")
    void testTokenValidationFailure() {
        String secret = "production-jwt-secret-key-that-is-at-least-32-bytes-long-for-hmacsha256";
        JwtTokenProvider provider = createProvider(secret, 3600000);

        String token = provider.generateAccessTokenFromEmail("user@smartservice.com", 10L, "CUSTOMER");
        String tamperedToken = token + "invalid";

        assertFalse(provider.validateToken(tamperedToken));
    }

    @Test
    @DisplayName("5. Missing or null secret throws clear IllegalStateException")
    void testNullOrBlankSecretThrowsClearException() {
        JwtTokenProvider providerNull = createProvider(null, 3600000);
        IllegalStateException exNull = assertThrows(IllegalStateException.class, () ->
                providerNull.generateAccessTokenFromEmail("admin@smartservice.com", 1L, "ADMIN")
        );
        assertTrue(exNull.getMessage().contains("JWT secret key is not configured"));

        JwtTokenProvider providerBlank = createProvider("   ", 3600000);
        IllegalStateException exBlank = assertThrows(IllegalStateException.class, () ->
                providerBlank.generateAccessTokenFromEmail("admin@smartservice.com", 1L, "ADMIN")
        );
        assertTrue(exBlank.getMessage().contains("JWT secret key is not configured"));
    }

    @Test
    @DisplayName("6. Short secret under 32 bytes throws clear IllegalStateException")
    void testShortSecretThrowsClearException() {
        JwtTokenProvider providerShort = createProvider("too-short", 3600000);
        IllegalStateException exShort = assertThrows(IllegalStateException.class, () ->
                providerShort.generateAccessTokenFromEmail("admin@smartservice.com", 1L, "ADMIN")
        );
        assertTrue(exShort.getMessage().contains("at least 32 characters (256 bits) long"));
    }
}
