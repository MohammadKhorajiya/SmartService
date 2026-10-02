package com.smartservice.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String rawUrl = environment.getProperty("DATABASE_URL");
        if (rawUrl == null || rawUrl.isBlank()) {
            rawUrl = environment.getProperty("spring.datasource.url");
        }

        if (rawUrl == null || rawUrl.isBlank()) {
            return;
        }

        // If rawUrl is already a clean JDBC URL with no embedded credentials (@), do not modify it
        if (rawUrl.startsWith("jdbc:") && !rawUrl.contains("@")) {
            return;
        }

        // Strip leading "jdbc:" if present (e.g., "jdbc:postgresql://user:pass@host/db")
        String workingUrl = rawUrl.startsWith("jdbc:") ? rawUrl.substring(5) : rawUrl;

        // Verify it starts with postgres:// or postgresql://
        if (!workingUrl.startsWith("postgres://") && !workingUrl.startsWith("postgresql://")) {
            return;
        }

        try {
            // Remove scheme prefix "postgres://" or "postgresql://"
            String schemeStripped = workingUrl.replaceFirst("^(postgres|postgresql)://", "");
            if (schemeStripped.isBlank()) {
                return;
            }

            Map<String, Object> targetProps = new HashMap<>();
            String hostAndPath;

            if (schemeStripped.contains("@")) {
                int lastAt = schemeStripped.lastIndexOf('@');
                String userInfo = schemeStripped.substring(0, lastAt);
                hostAndPath = schemeStripped.substring(lastAt + 1);

                if (!userInfo.isEmpty()) {
                    int firstColon = userInfo.indexOf(':');
                    String rawUsername;
                    String rawPassword;
                    if (firstColon != -1) {
                        rawUsername = userInfo.substring(0, firstColon);
                        rawPassword = userInfo.substring(firstColon + 1);
                    } else {
                        rawUsername = userInfo;
                        rawPassword = "";
                    }

                    String username = URLDecoder.decode(rawUsername, StandardCharsets.UTF_8);
                    String password = URLDecoder.decode(rawPassword, StandardCharsets.UTF_8);

                    targetProps.put("spring.datasource.username", username);
                    targetProps.put("spring.datasource.password", password);
                }
            } else {
                hostAndPath = schemeStripped;
            }

            if (!hostAndPath.isBlank()) {
                String cleanJdbcUrl = "jdbc:postgresql://" + hostAndPath;
                targetProps.put("spring.datasource.url", cleanJdbcUrl);
            }

            if (!targetProps.isEmpty()) {
                environment.getPropertySources().addFirst(
                        new MapPropertySource("parsedCloudDatabaseUrlProperties", targetProps)
                );
            }
        } catch (Exception e) {
            // Silently fallback if format is unparseable
        }
    }
}
