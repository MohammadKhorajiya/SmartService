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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthenticationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("1. Customer registration creates user & customer record with valid JWT token and HttpOnly cookie")
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
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
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
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.role", is("TECHNICIAN")))
                .andExpect(jsonPath("$.data.technicianId", notNullValue()));
    }

    @Test
    @DisplayName("3. Login with seed admin credentials returns valid JWT token and Set-Cookie")
    void testLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@smartservice.com");
        request.setPassword("Password@123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
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
    @DisplayName("5. Refresh token exchanges valid refresh cookie for new access token & rotates refresh token")
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
                .andExpect(header().exists("Set-Cookie"))
                .andReturn();

        jakarta.servlet.http.Cookie refreshCookie = regResult.getResponse().getCookie("refreshToken");

        // Perform refresh using cookie
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()));
    }

    @Test
    @DisplayName("6. Reuse of revoked refresh token triggers reuse detection alert")
    void testRevokedRefreshTokenReuseDetection() throws Exception {
        RegisterRequest regReq = new RegisterRequest();
        regReq.setEmail("reuse.user@smartservice.com");
        regReq.setPassword("Password@123");
        regReq.setFullName("Reuse User");
        regReq.setRole(Role.CUSTOMER);

        MvcResult regResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isOk())
                .andReturn();

        jakarta.servlet.http.Cookie initialCookie = regResult.getResponse().getCookie("refreshToken");

        // Rotate token once -> initialCookie is now marked revoked
        mockMvc.perform(post("/api/v1/auth/refresh").cookie(initialCookie))
                .andExpect(status().isOk());

        // Attempting to reuse initialCookie (now revoked) should trigger reuse detection error
        mockMvc.perform(post("/api/v1/auth/refresh").cookie(initialCookie))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("TOKEN_REUSE_DETECTED")));
    }

    @Test
    @DisplayName("7. Profile password update fails without correct current password")
    void testProfilePasswordUpdateFailsWithoutCorrectCurrentPassword() throws Exception {
        RegisterRequest regReq = new RegisterRequest();
        regReq.setEmail("pwd.user@smartservice.com");
        regReq.setPassword("Password@123");
        regReq.setFullName("Password User");
        regReq.setRole(Role.CUSTOMER);

        MvcResult regResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isOk())
                .andReturn();

        String body = regResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(body).path("data").path("accessToken").asText();

        com.smartservice.domain.auth.dto.UpdateProfileRequest updateReq = new com.smartservice.domain.auth.dto.UpdateProfileRequest();
        updateReq.setFullName("Password User");
        updateReq.setCurrentPassword("WrongPassword");
        updateReq.setNewPassword("NewPassword@123");

        mockMvc.perform(put("/api/v1/auth/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("INVALID_CURRENT_PASSWORD")));
    }

    @Test
    @DisplayName("8. Profile password update succeeds with valid current password")
    void testProfilePasswordUpdateSucceedsWithValidCurrentPassword() throws Exception {
        RegisterRequest regReq = new RegisterRequest();
        regReq.setEmail("pwd.success@smartservice.com");
        regReq.setPassword("Password@123");
        regReq.setFullName("Password Success User");
        regReq.setRole(Role.CUSTOMER);

        MvcResult regResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isOk())
                .andReturn();

        String body = regResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(body).path("data").path("accessToken").asText();

        com.smartservice.domain.auth.dto.UpdateProfileRequest updateReq = new com.smartservice.domain.auth.dto.UpdateProfileRequest();
        updateReq.setFullName("Password Success User");
        updateReq.setCurrentPassword("Password@123");
        updateReq.setNewPassword("NewPassword@123");

        mockMvc.perform(put("/api/v1/auth/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("9. Unauthenticated access to protected endpoints returns 401 Unauthorized")
    void testUnauthenticatedAccessRejected() throws Exception {
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("10. State-changing request with XSRF-TOKEN cookie but missing X-CSRF-Token header returns 403 Forbidden")
    void testCsrfValidationFailure() throws Exception {
        jakarta.servlet.http.Cookie csrfCookie = new jakarta.servlet.http.Cookie("XSRF-TOKEN", "test-csrf-token");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(csrfCookie))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("CSRF_ERROR")));
    }

    @Test
    @DisplayName("11. State-changing request with XSRF-TOKEN cookie and matching X-CSRF-Token header passes CSRF validation")
    void testCsrfValidationSuccess() throws Exception {
        jakarta.servlet.http.Cookie csrfCookie = new jakarta.servlet.http.Cookie("XSRF-TOKEN", "test-csrf-token");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(csrfCookie)
                        .header("X-CSRF-Token", "test-csrf-token"))
                .andExpect(status().isBadRequest());
    }
}
