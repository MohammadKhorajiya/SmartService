package com.smartservice;

import com.smartservice.config.SeedPasswordRepairInitializer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class SmartServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner initSeedPasswords(SeedPasswordRepairInitializer seedPasswordRepairInitializer) {
        return args -> seedPasswordRepairInitializer.repairSeedPasswords();
    }
}

