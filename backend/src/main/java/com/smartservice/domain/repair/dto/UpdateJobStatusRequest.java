package com.smartservice.domain.repair.dto;

import com.smartservice.domain.repair.RepairJobStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateJobStatusRequest {

    @NotNull(message = "New status is required")
    private RepairJobStatus newStatus;

    private String remarks;
}
