package com.smartservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseUrlEnvironmentPostProcessorTest {

    @Test
    void testPostProcessEnvironmentWithRenderUrl() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("DATABASE_URL", "postgresql://smartserviceprod:2Vwr5eyVGZjCVaaqKBYXFKrgmtQp6Xy8@dpg-davutk0u01pc73885jf0-a/smartservicedb");

        DatabaseUrlEnvironmentPostProcessor processor = new DatabaseUrlEnvironmentPostProcessor();
        processor.postProcessEnvironment(env, null);

        assertEquals("jdbc:postgresql://dpg-davutk0u01pc73885jf0-a:5432/smartservicedb", env.getProperty("spring.datasource.url"));
        assertEquals("smartserviceprod", env.getProperty("spring.datasource.username"));
        assertEquals("2Vwr5eyVGZjCVaaqKBYXFKrgmtQp6Xy8", env.getProperty("spring.datasource.password"));
    }

    @Test
    void testPostProcessEnvironmentWithPostgresSchemeAndPort() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("DATABASE_URL", "postgres://user:pass@host.example.com:5433/mydb");

        DatabaseUrlEnvironmentPostProcessor processor = new DatabaseUrlEnvironmentPostProcessor();
        processor.postProcessEnvironment(env, null);

        assertEquals("jdbc:postgresql://host.example.com:5433/mydb", env.getProperty("spring.datasource.url"));
        assertEquals("user", env.getProperty("spring.datasource.username"));
        assertEquals("pass", env.getProperty("spring.datasource.password"));
    }
}
