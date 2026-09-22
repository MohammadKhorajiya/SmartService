package com.smartservice.domain.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VerifyPaymentRequest {

    @NotNull(message = "Invoice ID is required")
    private Long invoiceId;

    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
    private String paymentMethod; // ONLINE, CASH, UPI
}
