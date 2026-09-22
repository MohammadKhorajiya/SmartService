package com.smartservice.domain.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSummaryDTO {

    private long totalCustomers;
    private long activeRepairs;
    private long pendingEstimates;
    private long completedRepairs;
    private BigDecimal totalRevenue;
    private long lowStockCount;
    private Map<String, Long> repairsByStatus;
}
