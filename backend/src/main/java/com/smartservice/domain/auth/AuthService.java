package com.smartservice.domain.auth;

import com.smartservice.common.exception.BusinessRuleException;
import com.smartservice.common.exception.ResourceNotFoundException;
import com.smartservice.domain.auth.dto.AuthResponse;
import com.smartservice.domain.auth.dto.LoginRequest;
import com.smartservice.domain.auth.dto.RefreshTokenRequest;
import com.smartservice.domain.auth.dto.RegisterRequest;
import com.smartservice.domain.customer.Customer;
import com.smartservice.domain.customer.CustomerRepository;
import com.smartservice.domain.technician.Technician;
import com.smartservice.domain.technician.TechnicianRepository;
import com.smartservice.domain.user.Role;
import com.smartservice.domain.user.User;
import com.smartservice.domain.user.UserRepository;
import com.smartservice.domain.user.UserStatus;
import com.smartservice.security.jwt.JwtTokenProvider;
import com.smartservice.security.refreshToken.RefreshToken;
import com.smartservice.security.refreshToken.RefreshTokenRepository;
import com.smartservice.security.userDetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final TechnicianRepository technicianRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Value("${app.jwt.refresh-token-expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessRuleException("User account is inactive or suspended");
        }

        String accessToken = tokenProvider.generateAccessToken(authentication);
        RefreshToken refreshToken = createRefreshToken(user);

        Long customerId = customerRepository.findByUserId(user.getId()).map(Customer::getId).orElse(null);
        Long technicianId = technicianRepository.findByUserId(user.getId()).map(Technician::getId).orElse(null);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getTokenHash())
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .customerId(customerId)
                .technicianId(technicianId)
                .build();
    }

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BusinessRuleException("Email address is already in use", "EMAIL_ALREADY_EXISTS");
        }

        Role assignedRole = registerRequest.getRole() != null ? registerRequest.getRole() : Role.CUSTOMER;

        User user = User.builder()
                .email(registerRequest.getEmail())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .fullName(registerRequest.getFullName())
                .phone(registerRequest.getPhone())
                .role(assignedRole)
                .status(UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);

        Long customerId = null;
        Long technicianId = null;
        if (assignedRole == Role.CUSTOMER) {
            Customer customer = Customer.builder()
                    .user(user)
                    .name(user.getFullName())
                    .email(user.getEmail())
                    .phone(user.getPhone() != null ? user.getPhone() : "")
                    .build();
            customer = customerRepository.save(customer);
            customerId = customer.getId();
        } else if (assignedRole == Role.TECHNICIAN) {
            Technician technician = Technician.builder()
                    .user(user)
                    .name(user.getFullName())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .status("AVAILABLE")
                    .build();
            Technician savedTech = technicianRepository.save(technician);
            technicianId = savedTech.getId();
        }

        // Auto login after registration
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(registerRequest.getEmail(), registerRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.generateAccessToken(authentication);
        RefreshToken refreshToken = createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getTokenHash())
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .customerId(customerId)
                .technicianId(technicianId)
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        return refreshToken(request != null ? request.getRefreshToken() : null);
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenStr) {
        if (refreshTokenStr == null || refreshTokenStr.trim().isEmpty()) {
            throw new BusinessRuleException("Refresh token is required", "MISSING_REFRESH_TOKEN");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(refreshTokenStr.trim())
                .orElseThrow(() -> new BusinessRuleException("Invalid refresh token", "INVALID_REFRESH_TOKEN"));

        // REUSE DETECTION / COMPROMISE ALERT:
        // If a revoked refresh token is presented, revoke the entire token family for that user
        if (refreshToken.isRevoked()) {
            User user = refreshToken.getUser();
            refreshTokenRepository.deleteByUser(user);
            throw new BusinessRuleException("Security alert: Revoked refresh token reuse detected. Session terminated.", "TOKEN_REUSE_DETECTED");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new BusinessRuleException("Refresh token is expired", "EXPIRED_REFRESH_TOKEN");
        }

        // ROTATION: Revoke current token and issue a new refresh token
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        User user = refreshToken.getUser();
        RefreshToken newRefreshToken = createRefreshToken(user);

        String newAccessToken = tokenProvider.generateAccessTokenFromEmail(user.getEmail(), user.getId(), user.getRole().name());

        Long customerId = customerRepository.findByUserId(user.getId()).map(Customer::getId).orElse(null);
        Long technicianId = technicianRepository.findByUserId(user.getId()).map(Technician::getId).orElse(null);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getTokenHash())
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .customerId(customerId)
                .technicianId(technicianId)
                .build();
    }

    @Transactional
    public void logout(String refreshTokenStr) {
        if (refreshTokenStr != null) {
            refreshTokenRepository.findByTokenHash(refreshTokenStr).ifPresent(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            });
        }
    }

    @Transactional(readOnly = true)
    public com.smartservice.domain.auth.dto.UserProfileDTO getCurrentProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessRuleException("User is not authenticated");
        }

        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        String address = customerRepository.findByUserId(user.getId())
                .map(Customer::getAddress)
                .orElse(null);

        return com.smartservice.domain.auth.dto.UserProfileDTO.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(address)
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Transactional
    public com.smartservice.domain.auth.dto.UserProfileDTO updateProfile(com.smartservice.domain.auth.dto.UpdateProfileRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessRuleException("User is not authenticated");
        }

        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName().trim());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }

        if (request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword().trim()));
        }

        userRepository.save(user);

        customerRepository.findByUserId(user.getId()).ifPresent(customer -> {
            customer.setName(user.getFullName());
            if (user.getPhone() != null) customer.setPhone(user.getPhone());
            if (request.getAddress() != null) customer.setAddress(request.getAddress().trim());
            customerRepository.save(customer);
        });

        String address = customerRepository.findByUserId(user.getId())
                .map(Customer::getAddress)
                .orElse(null);

        return com.smartservice.domain.auth.dto.UserProfileDTO.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(address)
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpirationMs))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(token);
    }
}
