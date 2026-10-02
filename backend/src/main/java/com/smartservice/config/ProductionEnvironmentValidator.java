package com.smartservice.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

@Slf4j
@Configuration
@Profile("prod")
@RequiredArgsConstructor
public class ProductionEnvironmentValidator {

    private final Environment environment;

    @PostConstruct
    public void validateProductionEnvironment() {
        log.info("Validating production environment configuration...");

        validateProperty("spring.datasource.url", "DATABASE_URL");
        validateProperty("spring.datasource.username", "DATABASE_USERNAME");
        validateProperty("spring.datasource.password", "DATABASE_PASSWORD");
        validateProperty("app.jwt.secret", "JWT_SECRET");

        String dbUrl = environment.getProperty("spring.datasource.url");
        if (dbUrl != null) {
            if (dbUrl.toLowerCase().contains("jdbc:h2")) {
                throw new IllegalStateException("CRITICAL SECURITY ERROR: Production profile cannot use H2 database URL: " + dbUrl);
            }
            if (dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://")) {
                throw new IllegalStateException("CRITICAL PRODUCTION CONFIGURATION ERROR: 'DATABASE_URL' (" + dbUrl
                        + ") must be a valid JDBC URL starting with 'jdbc:postgresql://' (or 'jdbc:postgres://'). "
                        + "Standard PostgreSQL URIs (e.g., 'postgres://...') are rejected by org.postgresql.Driver.");
            }
        }

        log.info("Production environment configuration validation passed successfully.");
    }

    private void validateProperty(String propertyKey, String envVarName) {
        String value = environment.getProperty(propertyKey);
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException("CRITICAL PRODUCTION CONFIGURATION ERROR: Required property '"
                    + propertyKey + "' (" + envVarName + ") is missing or empty.");
        }
    }
}
