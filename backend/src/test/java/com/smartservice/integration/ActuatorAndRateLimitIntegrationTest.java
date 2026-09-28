package com.smartservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartservice.domain.auth.dto.LoginRequest;
import com.smartservice.security.ratelimit.RateLimitingFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ActuatorAndRateLimitIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("1. Actuator health endpoint is exposed and returns UP without authentication")
    void testActuatorHealthEndpointExposed() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")));
    }

    @Test
    @DisplayName("2. Sensitive Actuator endpoints (e.g. /actuator/env, /actuator/beans) are protected/not exposed")
    void testSensitiveActuatorEndpointsNotExposed() throws Exception {
        mockMvc.perform(get("/actuator/env"))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/actuator/beans"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("3. Excessive authentication attempts are rate-limited with 429 Too Many Requests")
    void testAuthenticationRateLimiting() throws Exception {
        String testIp = "192.168.1.200";
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@smartservice.com");
        loginRequest.setPassword("WrongPassword123!");

        String jsonContent = objectMapper.writeValueAsString(loginRequest);

        // Perform requests up to configured limit (20)
        for (int i = 0; i < 20; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .header("X-Forwarded-For", testIp)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isUnauthorized());
        }

        // The 21st request from the same IP within the same minute should be rejected with HTTP 429
        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Forwarded-For", testIp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.code", is("TOO_MANY_REQUESTS")));
    }

    @Test
    @DisplayName("4. Rate limiting on one IP does not affect another IP")
    void testRateLimitingIsIpSpecific() throws Exception {
        String testIp = "192.168.1.201";
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@smartservice.com");
        loginRequest.setPassword("Password@123");

        // Request from different IP should succeed with 200 OK
        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Forwarded-For", testIp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }
}
