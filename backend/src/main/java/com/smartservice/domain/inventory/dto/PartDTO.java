package com.smartservice.domain.inventory.dto;

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
public class PartDTO {

    private Long id;
    private String sku;
    private String name;
    private String category;
    private String compatibleDevice;
    private BigDecimal purchasePrice;
    private BigDecimal sellingPrice;
    private Integer quantityInStock;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private Integer reorderLevel;
    private boolean active;
    private LocalDateTime createdAt;
}
