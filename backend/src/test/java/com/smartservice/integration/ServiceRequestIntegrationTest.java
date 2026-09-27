package com.smartservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartservice.domain.auth.dto.LoginRequest;
import com.smartservice.domain.auth.dto.RegisterRequest;
import com.smartservice.domain.device.dto.CreateDeviceRequest;
import com.smartservice.domain.servicerequest.dto.CreateServiceRequest;
import com.smartservice.domain.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ServiceRequestIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String customerToken1;
    private Long customerId1;
    private Long deviceId1;
    private String customerToken2;

    @BeforeEach
    void setUp() throws Exception {
        // Admin token
        LoginRequest adminLogin = new LoginRequest();
        adminLogin.setEmail("admin@smartservice.com");
        adminLogin.setPassword("Password@123");

        MvcResult adminRes = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();
        adminToken = objectMapper.readTree(adminRes.getResponse().getContentAsString()).path("data").path("accessToken").asText();

        // Customer 1
        RegisterRequest c1Reg = new RegisterRequest();
        c1Reg.setEmail("sr.cust1." + System.currentTimeMillis() + "@gmail.com");
        c1Reg.setPassword("Password@123");
        c1Reg.setFullName("SR Customer One");
        c1Reg.setRole(Role.CUSTOMER);

        MvcResult c1Res = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(c1Reg)))
                .andExpect(status().isOk())
                .andReturn();
        var c1Json = objectMapper.readTree(c1Res.getResponse().getContentAsString()).path("data");
        customerToken1 = c1Json.path("accessToken").asText();
        customerId1 = c1Json.path("customerId").asLong();

        // Register device for customer 1
        CreateDeviceRequest devReq = new CreateDeviceRequest();
        devReq.setCustomerId(customerId1);
        devReq.setBrand("Apple");
        devReq.setModel("MacBook Pro");
        devReq.setDeviceType("Laptop");

        MvcResult devRes = mockMvc.perform(post("/api/v1/devices")
                        .header("Authorization", "Bearer " + customerToken1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(devReq)))
                .andExpect(status().isOk())
                .andReturn();
        deviceId1 = objectMapper.readTree(devRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Customer 2
        RegisterRequest c2Reg = new RegisterRequest();
        c2Reg.setEmail("sr.cust2." + System.currentTimeMillis() + "@gmail.com");
        c2Reg.setPassword("Password@123");
        c2Reg.setFullName("SR Customer Two");
        c2Reg.setRole(Role.CUSTOMER);

        MvcResult c2Res = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(c2Reg)))
                .andExpect(status().isOk())
                .andReturn();
        customerToken2 = objectMapper.readTree(c2Res.getResponse().getContentAsString()).path("data").path("accessToken").asText();
    }

    @Test
    @DisplayName("1. Customer creates service request for owned device")
    void testCustomerCanCreateServiceRequest() throws Exception {
        CreateServiceRequest request = new CreateServiceRequest();
        request.setDeviceId(deviceId1);
        request.setProblemTitle("Screen flickering");
        request.setDescription("Display flickers when opening lid");
        request.setPriority("HIGH");

        mockMvc.perform(post("/api/v1/service-requests")
                        .header("Authorization", "Bearer " + customerToken1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.problemTitle", is("Screen flickering")))
                .andExpect(jsonPath("$.data.status", is("REQUESTED")));
    }

    @Test
    @DisplayName("2. Staff can access and list all service requests")
    void testStaffCanAccessServiceRequests() throws Exception {
        mockMvc.perform(get("/api/v1/service-requests")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", notNullValue()));
    }

    @Test
    @DisplayName("3. Customer cannot access another customer's service request details")
    void testCustomerCannotAccessOtherCustomerServiceRequest() throws Exception {
        // Customer 1 creates a request
        CreateServiceRequest request = new CreateServiceRequest();
        request.setDeviceId(deviceId1);
        request.setProblemTitle("Keyboard broken");

        MvcResult srRes = mockMvc.perform(post("/api/v1/service-requests")
                        .header("Authorization", "Bearer " + customerToken1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        Long srId = objectMapper.readTree(srRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Customer 2 attempts to fetch Customer 1's service request
        mockMvc.perform(get("/api/v1/service-requests/" + srId)
                        .header("Authorization", "Bearer " + customerToken2))
                .andExpect(status().isForbidden());
    }
}
