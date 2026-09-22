package com.smartservice.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentOrderResponse {
    private String orderId;
    private Long invoiceId;
    private String invoiceNumber;
    private BigDecimal amount;
    private String currency;
    private String keyId;
}
