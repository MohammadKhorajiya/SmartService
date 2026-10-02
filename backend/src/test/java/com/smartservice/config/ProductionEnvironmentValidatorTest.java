package com.smartservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.*;

class ProductionEnvironmentValidatorTest {

    @Test
    void testValidationPassesWithValidProperties() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/smartservicedb");
        env.setProperty("spring.datasource.username", "postgres");
        env.setProperty("spring.datasource.password", "secret");
        env.setProperty("app.jwt.secret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");

        ProductionEnvironmentValidator validator = new ProductionEnvironmentValidator(env);
        assertDoesNotThrow(validator::validateProductionEnvironment);
    }

    @Test
    void testValidationFailsWhenH2UrlProvided() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("spring.datasource.url", "jdbc:h2:mem:testdb");
        env.setProperty("spring.datasource.username", "sa");
        env.setProperty("spring.datasource.password", "password");
        env.setProperty("app.jwt.secret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");

        ProductionEnvironmentValidator validator = new ProductionEnvironmentValidator(env);
        IllegalStateException ex = assertThrows(IllegalStateException.class, validator::validateProductionEnvironment);
        assertTrue(ex.getMessage().contains("H2 database URL"));
    }

    @Test
    void testValidationFailsWhenNonJdbcPostgresUrlProvided() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("spring.datasource.url", "postgres://user:pass@host:5432/db");
        env.setProperty("spring.datasource.username", "user");
        env.setProperty("spring.datasource.password", "pass");
        env.setProperty("app.jwt.secret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");

        ProductionEnvironmentValidator validator = new ProductionEnvironmentValidator(env);
        IllegalStateException ex = assertThrows(IllegalStateException.class, validator::validateProductionEnvironment);
        assertTrue(ex.getMessage().contains("must be a valid JDBC URL starting with 'jdbc:postgresql://'"));
    }

    @Test
    void testValidationFailsWhenRequiredPropertyMissing() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/smartservicedb");
        env.setProperty("spring.datasource.username", "postgres");
        // missing password and jwt secret

        ProductionEnvironmentValidator validator = new ProductionEnvironmentValidator(env);
        assertThrows(IllegalStateException.class, validator::validateProductionEnvironment);
    }
}
