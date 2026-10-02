package com.smartservice.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String rawUrl = environment.getProperty("DATABASE_URL");
        if (rawUrl == null || rawUrl.isBlank()) {
            rawUrl = environment.getProperty("spring.datasource.url");
        }

        if (rawUrl != null && (rawUrl.startsWith("postgres://") || rawUrl.startsWith("postgresql://"))) {
            try {
                String httpUrl = rawUrl.replaceFirst("^(postgres|postgresql)://", "http://");
                URI uri = new URI(httpUrl);

                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                String path = uri.getPath();
                String query = uri.getRawQuery();

                StringBuilder jdbcUrlBuilder = new StringBuilder("jdbc:postgresql://");
                jdbcUrlBuilder.append(host).append(":").append(port).append(path);
                if (query != null && !query.isBlank()) {
                    jdbcUrlBuilder.append("?").append(query);
                }

                Map<String, Object> targetProps = new HashMap<>();
                targetProps.put("spring.datasource.url", jdbcUrlBuilder.toString());

                if (uri.getUserInfo() != null) {
                    String[] userInfo = uri.getUserInfo().split(":", 2);
                    if (userInfo.length >= 1 && !userInfo[0].isEmpty()) {
                        targetProps.put("spring.datasource.username", userInfo[0]);
                    }
                    if (userInfo.length >= 2 && !userInfo[1].isEmpty()) {
                        targetProps.put("spring.datasource.password", userInfo[1]);
                    }
                }

                environment.getPropertySources().addFirst(
                        new MapPropertySource("parsedCloudDatabaseUrlProperties", targetProps)
                );
            } catch (Exception e) {
                // Silently fallback if format is unparseable
            }
        }
    }
}
