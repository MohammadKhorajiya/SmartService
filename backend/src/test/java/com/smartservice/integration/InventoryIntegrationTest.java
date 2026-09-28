package com.smartservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartservice.domain.auth.dto.LoginRequest;
import com.smartservice.domain.inventory.dto.CreatePartRequest;
import com.smartservice.domain.inventory.dto.StockAdjustmentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class InventoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        LoginRequest adminLogin = new LoginRequest();
        adminLogin.setEmail("admin@smartservice.com");
        adminLogin.setPassword("Password@123");

        MvcResult adminRes = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();
        adminToken = objectMapper.readTree(adminRes.getResponse().getContentAsString()).path("data").path("accessToken").asText();
    }

    @Test
    @DisplayName("1. Admin creates inventory part and performs manual stock adjustment")
    void testCreatePartAndAdjustStock() throws Exception {
        String sku = "SKU-TEST-" + System.currentTimeMillis();
        CreatePartRequest req = new CreatePartRequest();
        req.setSku(sku);
        req.setName("OLED Display Test Part");
        req.setCategory("Screen");
        req.setPurchasePrice(new BigDecimal("100.00"));
        req.setSellingPrice(new BigDecimal("250.00"));
        req.setQuantityInStock(10);
        req.setReorderLevel(3);

        MvcResult partRes = mockMvc.perform(post("/api/v1/inventory/parts")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantityInStock", is(10)))
                .andReturn();

        Long partId = objectMapper.readTree(partRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Adjust stock +5
        StockAdjustmentRequest adjReq = new StockAdjustmentRequest();
        adjReq.setPartId(partId);
        adjReq.setQuantity(5);
        adjReq.setTransactionType("PURCHASE");
        adjReq.setNotes("Added extra stock");

        mockMvc.perform(post("/api/v1/inventory/adjust-stock")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adjReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantityInStock", is(15)));
    }

    @Test
    @DisplayName("2. Stock adjustment attempting negative stock levels throws 400 NEGATIVE_STOCK")
    void testNegativeStockAdjustmentRejected() throws Exception {
        String sku = "SKU-NEG-" + System.currentTimeMillis();
        CreatePartRequest req = new CreatePartRequest();
        req.setSku(sku);
        req.setName("Limited Component");
        req.setCategory("Chip");
        req.setPurchasePrice(new BigDecimal("50.00"));
        req.setSellingPrice(new BigDecimal("120.00"));
        req.setQuantityInStock(2);

        MvcResult partRes = mockMvc.perform(post("/api/v1/inventory/parts")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        Long partId = objectMapper.readTree(partRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Attempt to deduct -10 from stock of 2
        StockAdjustmentRequest adjReq = new StockAdjustmentRequest();
        adjReq.setPartId(partId);
        adjReq.setQuantity(-10);
        adjReq.setTransactionType("ADJUSTMENT");

        mockMvc.perform(post("/api/v1/inventory/adjust-stock")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adjReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("NEGATIVE_STOCK")));
    }
}
