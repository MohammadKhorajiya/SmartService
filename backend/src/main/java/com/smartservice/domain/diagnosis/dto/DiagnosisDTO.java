package com.smartservice.domain.diagnosis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiagnosisDTO {

    private Long id;
    private Long repairJobId;
    private String jobNumber;
    private Long technicianId;
    private String technicianName;
    private String symptoms;
    private String findings;
    private String recommendedRepair;
    private BigDecimal estimatedLaborCost;
    private String notes;
    private LocalDateTime createdAt;
}
