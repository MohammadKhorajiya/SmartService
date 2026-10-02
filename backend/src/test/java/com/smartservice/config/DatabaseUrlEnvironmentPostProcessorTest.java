package com.smartservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseUrlEnvironmentPostProcessorTest {

    @Test
    void testPostgresqlSchemeWithDefaultPort() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("DATABASE_URL", "postgresql://testuser:testpass@dpg-dummyhost-a/smartservicedb");

        DatabaseUrlEnvironmentPostProcessor processor = new DatabaseUrlEnvironmentPostProcessor();
        processor.postProcessEnvironment(env, null);

        String generatedUrl = env.getProperty("spring.datasource.url");
        assertNotNull(generatedUrl);
        assertEquals("jdbc:postgresql://dpg-dummyhost-a/smartservicedb", generatedUrl);
        assertEquals("testuser", env.getProperty("spring.datasource.username"));
        assertEquals("testpass", env.getProperty("spring.datasource.password"));

        // Explicit assertions: URL MUST NOT contain embedded credentials or @
        assertFalse(generatedUrl.contains("testuser"));
        assertFalse(generatedUrl.contains("testpass"));
        assertFalse(generatedUrl.contains("@"));
    }

    @Test
    void testPostgresSchemeWithExplicitPort() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("DATABASE_URL", "postgres://testuser:testpass@dbhost.example.com:5433/mydb");

        DatabaseUrlEnvironmentPostProcessor processor = new DatabaseUrlEnvironmentPostProcessor();
        processor.postProcessEnvironment(env, null);

        String generatedUrl = env.getProperty("spring.datasource.url");
        assertNotNull(generatedUrl);
        assertEquals("jdbc:postgresql://dbhost.example.com:5433/mydb", generatedUrl);
        assertEquals("testuser", env.getProperty("spring.datasource.username"));
        assertEquals("testpass", env.getProperty("spring.datasource.password"));

        assertFalse(generatedUrl.contains("testuser"));
        assertFalse(generatedUrl.contains("testpass"));
        assertFalse(generatedUrl.contains("@"));
    }

    @Test
    void testPostgresqlSchemeWithExplicitPort5432() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("DATABASE_URL", "postgresql://testuser:testpass@dbhost.example.com:5432/mydb");

        DatabaseUrlEnvironmentPostProcessor processor = new DatabaseUrlEnvironmentPostProcessor();
        processor.postProcessEnvironment(env, null);

        String generatedUrl = env.getProperty("spring.datasource.url");
        assertNotNull(generatedUrl);
        assertEquals("jdbc:postgresql://dbhost.example.com:5432/mydb", generatedUrl);
        assertEquals("testuser", env.getProperty("spring.datasource.username"));
        assertEquals("testpass", env.getProperty("spring.datasource.password"));

        assertFalse(generatedUrl.contains("testuser"));
        assertFalse(generatedUrl.contains("testpass"));
        assertFalse(generatedUrl.contains("@"));
    }

    @Test
    void testPasswordContainingSpecialUrlCharacters() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("DATABASE_URL", "postgresql://user%40domain:p%40ss%23word%21@dbhost.example.com:5432/mydb");

        DatabaseUrlEnvironmentPostProcessor processor = new DatabaseUrlEnvironmentPostProcessor();
        processor.postProcessEnvironment(env, null);

        String generatedUrl = env.getProperty("spring.datasource.url");
        assertNotNull(generatedUrl);
        assertEquals("jdbc:postgresql://dbhost.example.com:5432/mydb", generatedUrl);
        assertEquals("user@domain", env.getProperty("spring.datasource.username"));
        assertEquals("p@ss#word!", env.getProperty("spring.datasource.password"));

        assertFalse(generatedUrl.contains("user"));
        assertFalse(generatedUrl.contains("p@ss"));
        assertFalse(generatedUrl.contains("@dbhost"));
    }

    @Test
    void testJdbcPrefixedUrlWithEmbeddedCredentials() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("DATABASE_URL", "jdbc:postgresql://testuser:testpass@dpg-dummyhost-a/smartservicedb");

        DatabaseUrlEnvironmentPostProcessor processor = new DatabaseUrlEnvironmentPostProcessor();
        processor.postProcessEnvironment(env, null);

        String generatedUrl = env.getProperty("spring.datasource.url");
        assertNotNull(generatedUrl);
        assertEquals("jdbc:postgresql://dpg-dummyhost-a/smartservicedb", generatedUrl);
        assertEquals("testuser", env.getProperty("spring.datasource.username"));
        assertEquals("testpass", env.getProperty("spring.datasource.password"));

        assertFalse(generatedUrl.contains("testuser"));
        assertFalse(generatedUrl.contains("testpass"));
        assertFalse(generatedUrl.contains("@"));
    }

    @Test
    void testAlreadyCorrectJdbcFormattedUrlWithoutCredentials() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/smartservicedb");

        DatabaseUrlEnvironmentPostProcessor processor = new DatabaseUrlEnvironmentPostProcessor();
        processor.postProcessEnvironment(env, null);

        String generatedUrl = env.getProperty("spring.datasource.url");
        assertEquals("jdbc:postgresql://localhost:5432/smartservicedb", generatedUrl);
        assertNull(env.getProperty("spring.datasource.username"));
        assertNull(env.getProperty("spring.datasource.password"));
    }

    @Test
    void testMissingDatabaseUrlHandling() {
        MockEnvironment env = new MockEnvironment();

        DatabaseUrlEnvironmentPostProcessor processor = new DatabaseUrlEnvironmentPostProcessor();
        assertDoesNotThrow(() -> processor.postProcessEnvironment(env, null));

        assertNull(env.getProperty("spring.datasource.url"));
    }

    @Test
    void testInvalidDatabaseUrlHandling() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("DATABASE_URL", "not_a_valid_url");

        DatabaseUrlEnvironmentPostProcessor processor = new DatabaseUrlEnvironmentPostProcessor();
        assertDoesNotThrow(() -> processor.postProcessEnvironment(env, null));

        assertNull(env.getProperty("spring.datasource.url"));
    }

    @Test
    void testPreserveSslModeQueryParameters() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("DATABASE_URL", "postgresql://testuser:testpass@dbhost.example.com:5432/mydb?sslmode=require");

        DatabaseUrlEnvironmentPostProcessor processor = new DatabaseUrlEnvironmentPostProcessor();
        processor.postProcessEnvironment(env, null);

        String generatedUrl = env.getProperty("spring.datasource.url");
        assertNotNull(generatedUrl);
        assertEquals("jdbc:postgresql://dbhost.example.com:5432/mydb?sslmode=require", generatedUrl);
        assertEquals("testuser", env.getProperty("spring.datasource.username"));
        assertEquals("testpass", env.getProperty("spring.datasource.password"));

        assertFalse(generatedUrl.contains("testuser"));
        assertFalse(generatedUrl.contains("testpass"));
        assertFalse(generatedUrl.contains("@"));
    }
}
