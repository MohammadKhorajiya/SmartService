package com.smartservice.domain.diagnosis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateDiagnosisRequest {

    @NotNull(message = "Repair Job ID is required")
    private Long repairJobId;

    private String symptoms;

    @NotBlank(message = "Diagnosis findings are required")
    private String findings;

    private String recommendedRepair;
    private BigDecimal estimatedLaborCost;
    private String notes;
}
