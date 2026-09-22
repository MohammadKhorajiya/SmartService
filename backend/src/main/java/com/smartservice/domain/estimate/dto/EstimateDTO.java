package com.smartservice.domain.estimate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EstimateDTO {

    private Long id;
    private String estimateNumber;
    private Long repairJobId;
    private String jobNumber;
    private Long customerId;
    private String customerName;
    private String status;
    private BigDecimal totalPartsCost;
    private BigDecimal laborCost;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String rejectionReason;
    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime createdAt;
    private List<EstimateItemDTO> items;
}
