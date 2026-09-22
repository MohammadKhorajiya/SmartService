package com.smartservice.domain.estimate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateEstimateRequest {

    @NotNull(message = "Repair Job ID is required")
    private Long repairJobId;

    private BigDecimal laborCost;
    private BigDecimal taxRatePercentage; // e.g. 18.00 for 18% tax

    private List<EstimateItemRequest> items;

    @Data
    public static class EstimateItemRequest {
        private Long partId;
        private String itemDescription;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}
