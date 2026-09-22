package com.smartservice;

import com.smartservice.domain.user.User;
import com.smartservice.domain.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class SmartServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner initSeedPasswords(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String defaultHash = passwordEncoder.encode("Password@123");
            List<User> users = userRepository.findAll();
            for (User user : users) {
                if (user.getPasswordHash() == null || user.getPasswordHash().contains("8.UnVuG9HHgffUDAlk8qfOUVGkqRzgVym50cr0qlmCDAhK36Z8wgy")) {
                    user.setPasswordHash(defaultHash);
                    userRepository.save(user);
                }
            }
        };
    }
}

