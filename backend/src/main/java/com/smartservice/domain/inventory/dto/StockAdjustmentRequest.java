package com.smartservice.domain.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockAdjustmentRequest {

    @NotNull(message = "Part ID is required")
    private Long partId;

    @NotNull(message = "Quantity delta is required")
    private Integer quantity; // positive for addition, negative for deduction

    @NotBlank(message = "Transaction type is required")
    private String transactionType; // PURCHASE, ADJUSTMENT, RETURN

    private String notes;
}
