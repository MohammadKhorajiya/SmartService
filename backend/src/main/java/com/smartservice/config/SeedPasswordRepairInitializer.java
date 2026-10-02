package com.smartservice.config;

import com.smartservice.domain.user.User;
import com.smartservice.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeedPasswordRepairInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[ayb]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onApplicationReady() {
        repairSeedPasswords();
    }

    @Transactional
    public void repairSeedPasswords() {
        List<User> users = userRepository.findAll();
        int repairedCount = 0;
        for (User user : users) {
            String currentHash = user.getPasswordHash();
            if (isMalformedOrSeedHash(currentHash)) {
                String freshHash = passwordEncoder.encode("Password@123");
                user.setPasswordHash(freshHash);
                userRepository.saveAndFlush(user);
                repairedCount++;
                log.info("Repaired password hash for user email: {}", user.getEmail());
            }
        }
        if (repairedCount > 0) {
            log.info("Successfully repaired {} malformed user password hash(es).", repairedCount);
        }
    }

    public static boolean isMalformedOrSeedHash(String hash) {
        if (hash == null || hash.isBlank()) {
            return true;
        }
        if (hash.contains("8.UnVuG9HHgffUDAlk8qfOUVGkqRzgVym50cr0qlmCDAhK36Z8wgy")) {
            return true;
        }
        // BCrypt standard hash string length is exactly 60 characters
        if (hash.length() != 60) {
            return true;
        }
        // Standard BCrypt pattern validation
        return !BCRYPT_PATTERN.matcher(hash).matches();
    }
}
