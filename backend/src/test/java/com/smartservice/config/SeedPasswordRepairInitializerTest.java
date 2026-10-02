package com.smartservice.config;

import com.smartservice.domain.user.Role;
import com.smartservice.domain.user.User;
import com.smartservice.domain.user.UserRepository;
import com.smartservice.domain.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeedPasswordRepairInitializerTest {

    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;
    private SeedPasswordRepairInitializer repairInitializer;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        repairInitializer = new SeedPasswordRepairInitializer(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Should detect malformed 59-char seed hash as requiring repair")
    void testDetectMalformedSeedHash() {
        String malformedHash = "$2a$10$8.UnVuG9HHgffUDAlk8qfOUVGkqRzgVym50cr0qlmCDAhK36Z8wgy";
        assertTrue(SeedPasswordRepairInitializer.isMalformedOrSeedHash(malformedHash));
    }

    @Test
    @DisplayName("Should detect null or empty hash as requiring repair")
    void testDetectNullOrEmptyHash() {
        assertTrue(SeedPasswordRepairInitializer.isMalformedOrSeedHash(null));
        assertTrue(SeedPasswordRepairInitializer.isMalformedOrSeedHash(""));
        assertTrue(SeedPasswordRepairInitializer.isMalformedOrSeedHash("   "));
    }

    @Test
    @DisplayName("Should NOT mark valid 60-char BCrypt hash as malformed")
    void testValidBCryptHashNotMalformed() {
        String validHash = passwordEncoder.encode("Password@123");
        assertEquals(60, validHash.length());
        assertFalse(SeedPasswordRepairInitializer.isMalformedOrSeedHash(validHash));
    }

    @Test
    @DisplayName("Should repair malformed admin user password hash with valid Password@123 hash")
    void testRepairAdminUserPassword() {
        User adminUser = User.builder()
                .id(1L)
                .email("admin@smartservice.com")
                .passwordHash("$2a$10$8.UnVuG9HHgffUDAlk8qfOUVGkqRzgVym50cr0qlmCDAhK36Z8wgy")
                .fullName("System Administrator")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.findAll()).thenReturn(List.of(adminUser));

        repairInitializer.repairSeedPasswords();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).saveAndFlush(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertNotNull(savedUser.getPasswordHash());
        assertEquals(60, savedUser.getPasswordHash().length());
        assertTrue(passwordEncoder.matches("Password@123", savedUser.getPasswordHash()));
    }

    @Test
    @DisplayName("Should preserve valid custom user password hash without overwriting")
    void testPreserveValidCustomPassword() {
        String customValidHash = passwordEncoder.encode("MyCustomSecretPassword123!");
        User customUser = User.builder()
                .id(2L)
                .email("user@smartservice.com")
                .passwordHash(customValidHash)
                .fullName("Custom User")
                .role(Role.STAFF)
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.findAll()).thenReturn(List.of(customUser));

        repairInitializer.repairSeedPasswords();

        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Generate and output valid seed hash for SQL verification")
    void printValidBCryptHash() {
        String validHash = passwordEncoder.encode("Password@123");
        assertEquals(60, validHash.length());
        assertTrue(passwordEncoder.matches("Password@123", validHash));
        System.out.println("VALID_SEED_HASH_START:" + validHash + ":VALID_SEED_HASH_END");
    }
}
