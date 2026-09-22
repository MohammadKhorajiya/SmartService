package com.smartservice.domain.repair.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateRepairJobRequest {

    private Long serviceRequestId;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Device ID is required")
    private Long deviceId;

    private Long technicianId;
    private String priority; // LOW, MEDIUM, HIGH, URGENT
}
