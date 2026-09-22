package com.smartservice.domain.repair.dto;

import com.smartservice.domain.repair.RepairJobStatus;
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
public class RepairJobDTO {

    private Long id;
    private String jobNumber;
    private Long serviceRequestId;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private Long deviceId;
    private String deviceBrand;
    private String deviceModel;
    private String deviceSerialNumber;
    private String deviceImei;
    private Long technicianId;
    private String technicianName;
    private RepairJobStatus status;
    private String priority;
    private BigDecimal laborCost;
    private BigDecimal totalEstimatedCost;
    private BigDecimal totalActualCost;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime updatedAt;
}
