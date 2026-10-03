package com.smartservice.domain.auth;

import com.smartservice.common.dto.ApiResponse;
import com.smartservice.domain.auth.dto.AuthResponse;
import com.smartservice.domain.auth.dto.LoginRequest;
import com.smartservice.domain.auth.dto.RefreshTokenRequest;
import com.smartservice.domain.auth.dto.RegisterRequest;
import com.smartservice.security.jwt.CookieUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication & Authorization Endpoints")
public class AuthController {

    private final AuthService authService;
    private final CookieUtils cookieUtils;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and issue JWT tokens with HttpOnly cookie")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        ResponseCookie cookie = cookieUtils.createRefreshTokenCookie(response.getRefreshToken());
        response.setRefreshToken(null);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/register")
    @Operation(summary = "Register new customer or user account with HttpOnly cookie")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        ResponseCookie cookie = cookieUtils.createRefreshTokenCookie(response.getRefreshToken());
        response.setRefreshToken(null);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success(response, "User registered successfully"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Generate new access token using refresh cookie or request body")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @CookieValue(name = CookieUtils.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshTokenFromCookie,
            @RequestBody(required = false) RefreshTokenRequest request) {
        
        String tokenToRefresh = (refreshTokenFromCookie != null && !refreshTokenFromCookie.isBlank())
                ? refreshTokenFromCookie
                : (request != null ? request.getRefreshToken() : null);

        AuthResponse response = authService.refreshToken(tokenToRefresh);
        ResponseCookie newCookie = cookieUtils.createRefreshTokenCookie(response.getRefreshToken());
        response.setRefreshToken(null);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newCookie.toString())
                .body(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke user refresh token and clear HttpOnly cookie")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = CookieUtils.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshTokenFromCookie,
            @RequestBody(required = false) RefreshTokenRequest request) {
        
        String tokenToRevoke = (refreshTokenFromCookie != null && !refreshTokenFromCookie.isBlank())
                ? refreshTokenFromCookie
                : (request != null ? request.getRefreshToken() : null);

        if (tokenToRevoke != null && !tokenToRevoke.isBlank()) {
            authService.logout(tokenToRevoke);
        }

        ResponseCookie cleanCookie = cookieUtils.createCleanRefreshTokenCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .body(ApiResponse.success(null, "Logged out successfully"));
    }

    @GetMapping("/profile")
    @Operation(summary = "Get current logged-in user profile")
    public ResponseEntity<ApiResponse<com.smartservice.domain.auth.dto.UserProfileDTO>> getProfile() {
        com.smartservice.domain.auth.dto.UserProfileDTO profile = authService.getCurrentProfile();
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile retrieved successfully"));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update current logged-in user profile & password")
    public ResponseEntity<ApiResponse<com.smartservice.domain.auth.dto.UserProfileDTO>> updateProfile(
            @Valid @RequestBody com.smartservice.domain.auth.dto.UpdateProfileRequest request) {
        com.smartservice.domain.auth.dto.UserProfileDTO updated = authService.updateProfile(request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Profile updated successfully"));
    }
}
