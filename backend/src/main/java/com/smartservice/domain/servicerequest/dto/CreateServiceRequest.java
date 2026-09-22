package com.smartservice.domain.servicerequest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateServiceRequest {

    @NotNull(message = "Device ID is required")
    private Long deviceId;

    @NotBlank(message = "Problem title is required")
    private String problemTitle;

    private String description;
    private String priority; // LOW, MEDIUM, HIGH, URGENT
}
