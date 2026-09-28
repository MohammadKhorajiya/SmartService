package com.smartservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartservice.domain.auth.dto.LoginRequest;
import com.smartservice.domain.auth.dto.RegisterRequest;
import com.smartservice.domain.device.dto.CreateDeviceRequest;
import com.smartservice.domain.payment.dto.VerifyPaymentRequest;
import com.smartservice.domain.repair.dto.CreateRepairJobRequest;
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

class PaymentIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String customerToken1;
    private Long customerId1;
    private String customerToken2;
    private Long jobId1;
    private Long deviceId1;
    private Long invoiceId1;

    @BeforeEach
    void setUp() throws Exception {
        // Admin
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
        c1Reg.setEmail("pay.c1." + System.currentTimeMillis() + "@gmail.com");
        c1Reg.setPassword("Password@123");
        c1Reg.setFullName("Payment Customer One");
        c1Reg.setRole(Role.CUSTOMER);

        MvcResult c1Res = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(c1Reg)))
                .andExpect(status().isOk())
                .andReturn();
        var c1Json = objectMapper.readTree(c1Res.getResponse().getContentAsString()).path("data");
        customerToken1 = c1Json.path("accessToken").asText();
        customerId1 = c1Json.path("customerId").asLong();

        // Customer 2
        RegisterRequest c2Reg = new RegisterRequest();
        c2Reg.setEmail("pay.c2." + System.currentTimeMillis() + "@gmail.com");
        c2Reg.setPassword("Password@123");
        c2Reg.setFullName("Payment Customer Two");
        c2Reg.setRole(Role.CUSTOMER);

        MvcResult c2Res = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(c2Reg)))
                .andExpect(status().isOk())
                .andReturn();
        customerToken2 = objectMapper.readTree(c2Res.getResponse().getContentAsString()).path("data").path("accessToken").asText();

        // Device for C1
        CreateDeviceRequest devReq = new CreateDeviceRequest();
        devReq.setCustomerId(customerId1);
        devReq.setBrand("Apple");
        devReq.setModel("iPad Pro");
        devReq.setDeviceType("Tablet");

        MvcResult devRes = mockMvc.perform(post("/api/v1/devices")
                        .header("Authorization", "Bearer " + customerToken1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(devReq)))
                .andExpect(status().isOk())
                .andReturn();
        deviceId1 = objectMapper.readTree(devRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Repair Job for C1
        CreateRepairJobRequest jobReq = new CreateRepairJobRequest();
        jobReq.setCustomerId(customerId1);
        jobReq.setDeviceId(deviceId1);

        MvcResult jobRes = mockMvc.perform(post("/api/v1/repair-jobs")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobReq)))
                .andExpect(status().isOk())
                .andReturn();
        jobId1 = objectMapper.readTree(jobRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Generate Invoice
        MvcResult invRes = mockMvc.perform(post("/api/v1/invoices/job/" + jobId1)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        invoiceId1 = objectMapper.readTree(invRes.getResponse().getContentAsString()).path("data").path("id").asLong();
    }

    @Test
    @DisplayName("1. Customer creates payment order and verifies payment successfully")
    void testInvoiceGenerationAndPaymentOrder() throws Exception {
        // Customer 1 creates payment order
        mockMvc.perform(post("/api/v1/payments/create-order/" + invoiceId1)
                        .header("Authorization", "Bearer " + customerToken1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderId", notNullValue()));

        // Customer 1 verifies payment
        String txId = "pay_tx_" + System.currentTimeMillis();
        VerifyPaymentRequest verifyReq = new VerifyPaymentRequest();
        verifyReq.setInvoiceId(invoiceId1);
        verifyReq.setRazorpayOrderId("order_mock_123");
        verifyReq.setRazorpayPaymentId(txId);
        verifyReq.setPaymentMethod("ONLINE");

        mockMvc.perform(post("/api/v1/payments/verify")
                        .header("Authorization", "Bearer " + customerToken1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", is(true)));
    }

    @Test
    @DisplayName("2. Customer cannot create payment order for another customer's invoice (403 Forbidden)")
    void testCustomerCannotCreateOrderForOtherCustomerInvoice() throws Exception {
        // Customer 2 attempts to create payment order for Customer 1's invoice
        mockMvc.perform(post("/api/v1/payments/create-order/" + invoiceId1)
                        .header("Authorization", "Bearer " + customerToken2))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("3. Customer cannot verify payment for another customer's invoice (403 Forbidden)")
    void testCustomerCannotVerifyOtherCustomerInvoicePayment() throws Exception {
        VerifyPaymentRequest verifyReq = new VerifyPaymentRequest();
        verifyReq.setInvoiceId(invoiceId1);
        verifyReq.setRazorpayOrderId("order_mock_123");
        verifyReq.setRazorpayPaymentId("pay_tx_unauth");
        verifyReq.setPaymentMethod("ONLINE");

        mockMvc.perform(post("/api/v1/payments/verify")
                        .header("Authorization", "Bearer " + customerToken2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("4. Duplicate payment transaction ID is rejected with 400 DUPLICATE_PAYMENT_TRANSACTION")
    void testDuplicatePaymentTransactionRejected() throws Exception {
        // Create second job and invoice for Customer 1
        CreateRepairJobRequest jobReq2 = new CreateRepairJobRequest();
        jobReq2.setCustomerId(customerId1);
        jobReq2.setDeviceId(deviceId1);

        MvcResult jobRes2 = mockMvc.perform(post("/api/v1/repair-jobs")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobReq2)))
                .andExpect(status().isOk())
                .andReturn();
        Long jobId2 = objectMapper.readTree(jobRes2.getResponse().getContentAsString()).path("data").path("id").asLong();

        MvcResult invRes2 = mockMvc.perform(post("/api/v1/invoices/job/" + jobId2)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        Long invoiceId2 = objectMapper.readTree(invRes2.getResponse().getContentAsString()).path("data").path("id").asLong();

        String txId = "pay_dup_" + System.currentTimeMillis();
        VerifyPaymentRequest verifyReq1 = new VerifyPaymentRequest();
        verifyReq1.setInvoiceId(invoiceId1);
        verifyReq1.setRazorpayPaymentId(txId);
        verifyReq1.setPaymentMethod("ONLINE");

        // First verification
        mockMvc.perform(post("/api/v1/payments/verify")
                        .header("Authorization", "Bearer " + customerToken1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq1)))
                .andExpect(status().isOk());

        // Replay attempt with same transaction ID on second unpaid invoice
        VerifyPaymentRequest verifyReq2 = new VerifyPaymentRequest();
        verifyReq2.setInvoiceId(invoiceId2);
        verifyReq2.setRazorpayPaymentId(txId);
        verifyReq2.setPaymentMethod("ONLINE");

        mockMvc.perform(post("/api/v1/payments/verify")
                        .header("Authorization", "Bearer " + customerToken1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("DUPLICATE_PAYMENT_TRANSACTION")));
    }
}
