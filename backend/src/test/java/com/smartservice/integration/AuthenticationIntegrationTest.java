package com.smartservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartservice.domain.auth.dto.LoginRequest;
import com.smartservice.domain.auth.dto.RefreshTokenRequest;
import com.smartservice.domain.auth.dto.RegisterRequest;
import com.smartservice.domain.user.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthenticationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("1. Customer registration creates user & customer record with valid JWT token")
    void testCustomerRegistrationSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test.customer@smartservice.com");
        request.setPassword("Password@123");
        request.setFullName("Test Customer");
        request.setPhone("+18005550001");
        request.setRole(Role.CUSTOMER);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.data.email", is("test.customer@smartservice.com")))
                .andExpect(jsonPath("$.data.role", is("CUSTOMER")))
                .andExpect(jsonPath("$.data.customerId", notNullValue()));
    }

    @Test
    @DisplayName("2. Technician registration populates technicianId consistently")
    void testTechnicianRegistrationSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("tech.bob@smartservice.com");
        request.setPassword("Password@123");
        request.setFullName("Bob Technician");
        request.setPhone("+18005550002");
        request.setRole(Role.TECHNICIAN);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.role", is("TECHNICIAN")))
                .andExpect(jsonPath("$.data.technicianId", notNullValue()));
    }

    @Test
    @DisplayName("3. Login with seed admin credentials returns valid JWT token")
    void testLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@smartservice.com");
        request.setPassword("Password@123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.role", is("ADMIN")));
    }

    @Test
    @DisplayName("4. Login with invalid password returns 401 Unauthorized")
    void testLoginFailureInvalidPassword() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@smartservice.com");
        request.setPassword("WrongPassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("5. Refresh token exchanges valid refresh token for new access token")
    void testRefreshTokenSuccess() throws Exception {
        // Register user first
        RegisterRequest regReq = new RegisterRequest();
        regReq.setEmail("refresh.user@smartservice.com");
        regReq.setPassword("Password@123");
        regReq.setFullName("Refresh User");
        regReq.setRole(Role.CUSTOMER);

        MvcResult regResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isOk())
                .andReturn();

        String body = regResult.getResponse().getContentAsString();
        String refreshToken = objectMapper.readTree(body).path("data").path("refreshToken").asText();

        // Refresh token
        RefreshTokenRequest refreshReq = new RefreshTokenRequest();
        refreshReq.setRefreshToken(refreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken", notNullValue()));
    }

    @Test
    @DisplayName("6. Unauthenticated access to protected endpoints returns 401 Unauthorized")
    void testUnauthenticatedAccessRejected() throws Exception {
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isUnauthorized());
    }
}
