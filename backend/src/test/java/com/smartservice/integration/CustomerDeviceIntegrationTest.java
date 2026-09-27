package com.smartservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartservice.domain.auth.dto.LoginRequest;
import com.smartservice.domain.auth.dto.RegisterRequest;
import com.smartservice.domain.customer.dto.CreateCustomerRequest;
import com.smartservice.domain.device.dto.CreateDeviceRequest;
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

class CustomerDeviceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String customerToken1;
    private Long customerId1;
    private String customerToken2;
    private Long customerId2;

    @BeforeEach
    void setUp() throws Exception {
        // Admin login
        LoginRequest adminLogin = new LoginRequest();
        adminLogin.setEmail("admin@smartservice.com");
        adminLogin.setPassword("Password@123");

        MvcResult adminRes = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();
        adminToken = objectMapper.readTree(adminRes.getResponse().getContentAsString()).path("data").path("accessToken").asText();

        // Customer 1 registration
        RegisterRequest c1Reg = new RegisterRequest();
        c1Reg.setEmail("cust1." + System.currentTimeMillis() + "@gmail.com");
        c1Reg.setPassword("Password@123");
        c1Reg.setFullName("Customer One");
        c1Reg.setRole(Role.CUSTOMER);

        MvcResult c1Res = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(c1Reg)))
                .andExpect(status().isOk())
                .andReturn();
        var c1Json = objectMapper.readTree(c1Res.getResponse().getContentAsString()).path("data");
        customerToken1 = c1Json.path("accessToken").asText();
        customerId1 = c1Json.path("customerId").asLong();

        // Customer 2 registration
        RegisterRequest c2Reg = new RegisterRequest();
        c2Reg.setEmail("cust2." + System.currentTimeMillis() + "@gmail.com");
        c2Reg.setPassword("Password@123");
        c2Reg.setFullName("Customer Two");
        c2Reg.setRole(Role.CUSTOMER);

        MvcResult c2Res = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(c2Reg)))
                .andExpect(status().isOk())
                .andReturn();
        var c2Json = objectMapper.readTree(c2Res.getResponse().getContentAsString()).path("data");
        customerToken2 = c2Json.path("accessToken").asText();
        customerId2 = c2Json.path("customerId").asLong();
    }

    @Test
    @DisplayName("1. Admin/Staff can create customer profiles")
    void testStaffCanCreateCustomer() throws Exception {
        CreateCustomerRequest req = new CreateCustomerRequest();
        req.setName("New Walkin Customer");
        req.setEmail("walkin@gmail.com");
        req.setPhone("+18005559900");
        req.setAddress("100 Main St");

        mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name", is("New Walkin Customer")));
    }

    @Test
    @DisplayName("2. CUSTOMER role cannot create new customer profile (403 Forbidden)")
    void testCustomerCannotCreateCustomer() throws Exception {
        CreateCustomerRequest req = new CreateCustomerRequest();
        req.setName("Unauthorized Customer");
        req.setEmail("unauth@gmail.com");
        req.setPhone("+18005559901");

        mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", "Bearer " + customerToken1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("3. CUSTOMER role cannot search/list customer directory (403 Forbidden)")
    void testCustomerCannotAccessCustomerDirectory() throws Exception {
        mockMvc.perform(get("/api/v1/customers")
                        .header("Authorization", "Bearer " + customerToken1))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("4. CUSTOMER can create device for their own account")
    void testCustomerCanCreateDeviceForSelf() throws Exception {
        CreateDeviceRequest devReq = new CreateDeviceRequest();
        devReq.setCustomerId(customerId1);
        devReq.setBrand("Apple");
        devReq.setModel("iPhone 15 Pro");
        devReq.setSerialNumber("SN-IPH15-C1");
        devReq.setDeviceType("Mobile");

        mockMvc.perform(post("/api/v1/devices")
                        .header("Authorization", "Bearer " + customerToken1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(devReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.brand", is("Apple")));
    }

    @Test
    @DisplayName("5. CUSTOMER cannot create device for another customer (403/Unauthorized)")
    void testCustomerCannotCreateDeviceForOtherCustomer() throws Exception {
        CreateDeviceRequest devReq = new CreateDeviceRequest();
        devReq.setCustomerId(customerId2); // Trying to attach device to Customer 2
        devReq.setBrand("Samsung");
        devReq.setModel("Galaxy S24");
        devReq.setDeviceType("Mobile");

        mockMvc.perform(post("/api/v1/devices")
                        .header("Authorization", "Bearer " + customerToken1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(devReq)))
                .andExpect(status().isForbidden());
    }
}
