package com.smartservice.domain.auth;

import com.smartservice.common.dto.ApiResponse;
import com.smartservice.domain.auth.dto.AuthResponse;
import com.smartservice.domain.auth.dto.LoginRequest;
import com.smartservice.domain.auth.dto.RefreshTokenRequest;
import com.smartservice.domain.auth.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication & Authorization Endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and issue JWT tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/register")
    @Operation(summary = "Register new customer or user account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(response, "User registered successfully"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Generate new access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke user refresh token")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        if (request != null) {
            authService.logout(request.getRefreshToken());
        }
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
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
