package com.smartservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartservice.domain.auth.dto.LoginRequest;
import com.smartservice.domain.auth.dto.RegisterRequest;
import com.smartservice.domain.device.dto.CreateDeviceRequest;
import com.smartservice.domain.diagnosis.dto.CreateDiagnosisRequest;
import com.smartservice.domain.estimate.dto.ApproveRejectEstimateRequest;
import com.smartservice.domain.estimate.dto.CreateEstimateRequest;
import com.smartservice.domain.repair.RepairJobStatus;
import com.smartservice.domain.repair.dto.AssignTechnicianRequest;
import com.smartservice.domain.repair.dto.CreateRepairJobRequest;
import com.smartservice.domain.repair.dto.UpdateJobStatusRequest;
import com.smartservice.domain.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RepairWorkflowIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String techToken;
    private Long techId;
    private String customerToken;
    private Long customerId;
    private Long deviceId;

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

        // Register Customer
        RegisterRequest custReg = new RegisterRequest();
        custReg.setEmail("rw.cust." + System.currentTimeMillis() + "@gmail.com");
        custReg.setPassword("Password@123");
        custReg.setFullName("RW Customer");
        custReg.setRole(Role.CUSTOMER);

        MvcResult custRes = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(custReg)))
                .andExpect(status().isOk())
                .andReturn();
        var custJson = objectMapper.readTree(custRes.getResponse().getContentAsString()).path("data");
        customerToken = custJson.path("accessToken").asText();
        customerId = custJson.path("customerId").asLong();

        // Register Technician
        RegisterRequest techReg = new RegisterRequest();
        techReg.setEmail("rw.tech." + System.currentTimeMillis() + "@smartservice.com");
        techReg.setPassword("Password@123");
        techReg.setFullName("RW Tech");
        techReg.setRole(Role.TECHNICIAN);

        MvcResult techRes = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(techReg)))
                .andExpect(status().isOk())
                .andReturn();
        var techJson = objectMapper.readTree(techRes.getResponse().getContentAsString()).path("data");
        techToken = techJson.path("accessToken").asText();
        techId = techJson.path("technicianId").asLong();

        // Create Device for Customer
        CreateDeviceRequest devReq = new CreateDeviceRequest();
        devReq.setCustomerId(customerId);
        devReq.setBrand("Apple");
        devReq.setModel("iPhone 15");
        devReq.setDeviceType("Mobile");

        MvcResult devRes = mockMvc.perform(post("/api/v1/devices")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(devReq)))
                .andExpect(status().isOk())
                .andReturn();
        deviceId = objectMapper.readTree(devRes.getResponse().getContentAsString()).path("data").path("id").asLong();
    }

    @Test
    @DisplayName("1. Full repair job lifecycle: Create -> Assign -> Diagnose -> Estimate -> Approve -> In Repair")
    void testFullRepairJobLifecycle() throws Exception {
        // Step 1: Create Repair Job (Admin/Staff)
        CreateRepairJobRequest jobReq = new CreateRepairJobRequest();
        jobReq.setCustomerId(customerId);
        jobReq.setDeviceId(deviceId);
        jobReq.setPriority("HIGH");

        MvcResult jobRes = mockMvc.perform(post("/api/v1/repair-jobs")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("REQUESTED")))
                .andReturn();

        Long jobId = objectMapper.readTree(jobRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Step 2: Assign Technician (Admin)
        AssignTechnicianRequest assignReq = new AssignTechnicianRequest();
        assignReq.setTechnicianId(techId);
        mockMvc.perform(post("/api/v1/repair-jobs/" + jobId + "/assign-technician")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("ASSIGNED")));

        // Step 3: Tech submits diagnosis
        CreateDiagnosisRequest diagReq = new CreateDiagnosisRequest();
        diagReq.setRepairJobId(jobId);
        diagReq.setSymptoms("Cracked glass and uncalibrated touch");
        diagReq.setFindings("Display panel needs replacement");
        diagReq.setRecommendedRepair("Replace OLED screen assembly");
        diagReq.setEstimatedLaborCost(new BigDecimal("500.00"));

        mockMvc.perform(post("/api/v1/diagnoses")
                        .header("Authorization", "Bearer " + techToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(diagReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.findings", is("Display panel needs replacement")));

        // Step 4: Generate Estimate
        CreateEstimateRequest.EstimateItemRequest item = new CreateEstimateRequest.EstimateItemRequest();
        item.setItemDescription("OLED Display Assembly");
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("2500.00"));

        CreateEstimateRequest estReq = new CreateEstimateRequest();
        estReq.setRepairJobId(jobId);
        estReq.setLaborCost(new BigDecimal("500.00"));
        estReq.setItems(List.of(item));

        MvcResult estRes = mockMvc.perform(post("/api/v1/estimates")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("SENT")))
                .andReturn();

        Long estId = objectMapper.readTree(estRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Step 5: Customer Approves Estimate
        ApproveRejectEstimateRequest approveReq = new ApproveRejectEstimateRequest();
        approveReq.setApproved(true);

        mockMvc.perform(post("/api/v1/estimates/" + estId + "/approval")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("APPROVED")));

        // Step 6: Tech updates status to IN_REPAIR
        UpdateJobStatusRequest statusReq = new UpdateJobStatusRequest();
        statusReq.setNewStatus(RepairJobStatus.IN_REPAIR);
        statusReq.setRemarks("Starting repair process");

        mockMvc.perform(patch("/api/v1/repair-jobs/" + jobId + "/status")
                        .header("Authorization", "Bearer " + techToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("IN_REPAIR")));
    }

    @Test
    @DisplayName("2. Invalid repair job status transition throws 400 BusinessRuleException")
    void testInvalidStatusTransitionFails() throws Exception {
        // Create Job
        CreateRepairJobRequest jobReq = new CreateRepairJobRequest();
        jobReq.setCustomerId(customerId);
        jobReq.setDeviceId(deviceId);

        MvcResult jobRes = mockMvc.perform(post("/api/v1/repair-jobs")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobReq)))
                .andExpect(status().isOk())
                .andReturn();

        Long jobId = objectMapper.readTree(jobRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Attempt illegal transition directly from REQUESTED to COMPLETED
        UpdateJobStatusRequest illegalStatus = new UpdateJobStatusRequest();
        illegalStatus.setNewStatus(RepairJobStatus.COMPLETED);
        illegalStatus.setRemarks("Illegal skip");

        mockMvc.perform(patch("/api/v1/repair-jobs/" + jobId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(illegalStatus)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("INVALID_STATUS_TRANSITION")));
    }
}
