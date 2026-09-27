package com.smartservice.domain.payment;

import com.smartservice.common.dto.ApiResponse;
import com.smartservice.domain.payment.dto.PaymentOrderResponse;
import com.smartservice.domain.payment.dto.VerifyPaymentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Gateway Integration", description = "Endpoints for creating payment orders and server-side verification")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order/{invoiceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "Initialize payment order for invoice")
    public ResponseEntity<ApiResponse<PaymentOrderResponse>> createOrder(@PathVariable Long invoiceId) {
        PaymentOrderResponse response = paymentService.createPaymentOrder(invoiceId);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment order created"));
    }

    @PostMapping("/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "Server-side payment verification and receipt generation")
    public ResponseEntity<ApiResponse<Boolean>> verifyPayment(@Valid @RequestBody VerifyPaymentRequest request) {
        boolean verified = paymentService.verifyAndRecordPayment(request);
        return ResponseEntity.ok(ApiResponse.success(verified, "Payment verified successfully"));
    }
}
