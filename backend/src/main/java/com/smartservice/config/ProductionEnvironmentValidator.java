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
        // Check if mock payment mode is enabled
        String paymentMode = environment.getProperty("payment.mode", "");
        boolean isMockEnabled = "mock".equalsIgnoreCase(paymentMode) 
                || "true".equalsIgnoreCase(environment.getProperty("payment.fake.enabled", "false"));

        if (isMockEnabled) {
            log.info("✅ Mock payment mode enabled - payment configuration validator bypassed");
        } else {
            validateProperty("app.payment.key-id", "PAYMENT_KEY_ID");
            validateProperty("app.payment.key-secret", "PAYMENT_KEY_SECRET");

            String paymentKeyId = environment.getProperty("app.payment.key-id");
            if (!StringUtils.hasText(paymentKeyId)) {
                paymentKeyId = environment.getProperty("razorpay.key.id", "");
            }
            String paymentKeySecret = environment.getProperty("app.payment.key-secret");
            if (!StringUtils.hasText(paymentKeySecret)) {
                paymentKeySecret = environment.getProperty("razorpay.key.secret", "");
            }

            if (paymentKeyId != null && (paymentKeyId.toLowerCase().contains("rzp_test_mockkeyid") || paymentKeyId.toLowerCase().contains("test"))) {
                throw new IllegalStateException("CRITICAL PRODUCTION CONFIGURATION ERROR: Production profile cannot use mock payment key ID: " + paymentKeyId + ". Either enable mock mode (payment.mode=mock) or provide production keys.");
            }
            if (paymentKeySecret != null && (paymentKeySecret.toLowerCase().contains("mockkeysecret") || paymentKeySecret.toLowerCase().contains("test"))) {
                throw new IllegalStateException("CRITICAL PRODUCTION CONFIGURATION ERROR: Production profile cannot use mock payment key secret: " + paymentKeySecret + ". Either enable mock mode (payment.mode=mock) or provide production secret.");
            }
            log.info("✅ Payment configuration validation passed");
        }

        String dbUrl = environment.getProperty("spring.datasource.url");
        if (dbUrl != null) {
            if (dbUrl.toLowerCase().contains("jdbc:h2")) {
                throw new IllegalStateException("CRITICAL SECURITY ERROR: Production profile cannot use H2 database URL: " + dbUrl);
            }
            if (dbUrl.contains("@")) {
                throw new IllegalStateException("CRITICAL PRODUCTION CONFIGURATION ERROR: Resolved 'spring.datasource.url' (" + dbUrl
                        + ") contains embedded credentials ('@'). Username and password must be passed in separate properties.");
            }
            if (!dbUrl.startsWith("jdbc:postgresql://") && !dbUrl.startsWith("jdbc:postgres://")) {
                throw new IllegalStateException("CRITICAL PRODUCTION CONFIGURATION ERROR: Resolved 'spring.datasource.url' (" + dbUrl
                        + ") must be a valid JDBC URL starting with 'jdbc:postgresql://'.");
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
