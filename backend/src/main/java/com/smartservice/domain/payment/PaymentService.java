package com.smartservice.domain.payment;

import com.smartservice.common.exception.BusinessRuleException;
import com.smartservice.common.exception.ResourceNotFoundException;
import com.smartservice.common.exception.UnauthorizedAccessException;
import com.smartservice.common.util.SecurityUtils;
import com.smartservice.domain.inventory.InventoryService;
import com.smartservice.domain.invoice.Invoice;
import com.smartservice.domain.invoice.InvoiceRepository;
import com.smartservice.domain.payment.dto.PaymentOrderResponse;
import com.smartservice.domain.payment.dto.VerifyPaymentRequest;
import com.smartservice.domain.repair.RepairJob;
import com.smartservice.domain.repair.RepairJobRepository;
import com.smartservice.domain.repair.RepairJobStatus;
import com.smartservice.domain.repair.RepairJobStatusTransitionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final RepairJobRepository repairJobRepository;
    private final InventoryService inventoryService;
    private final RepairJobStatusTransitionValidator transitionValidator;

    @Value("${app.payment.key-id:rzp_test_mockKeyId}")
    private String keyId;

    @Value("${app.payment.key-secret:mockKeySecret}")
    private String keySecret;

    @Transactional
    public PaymentOrderResponse createPaymentOrder(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", invoiceId));

        if ("PAID".equalsIgnoreCase(invoice.getPaymentStatus())) {
            throw new BusinessRuleException("Invoice is already fully paid", "INVOICE_ALREADY_PAID");
        }

        // Validate Ownership: CUSTOMER can only create payment order for their own invoice
        if (SecurityUtils.isCustomer()) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (invoice.getCustomer().getUser() == null || !invoice.getCustomer().getUser().getId().equals(currentUserId)) {
                throw new UnauthorizedAccessException("You are only authorized to pay for your own invoice");
            }
        }

        String orderId = "order_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        return PaymentOrderResponse.builder()
                .orderId(orderId)
                .invoiceId(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .amount(invoice.getTotalAmount())
                .currency("INR")
                .keyId(keyId)
                .build();
    }

    @Transactional
    public boolean verifyAndRecordPayment(VerifyPaymentRequest request) {
        if (request == null || request.getInvoiceId() == null) {
            throw new BusinessRuleException("Invoice ID is required for payment verification", "INVALID_PAYMENT_REQUEST");
        }

        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", request.getInvoiceId()));

        if ("PAID".equalsIgnoreCase(invoice.getPaymentStatus())) {
            return true; // Already processed and paid
        }

        // Validate Ownership: CUSTOMER can only verify payment for their own invoice
        if (SecurityUtils.isCustomer()) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (invoice.getCustomer().getUser() == null || !invoice.getCustomer().getUser().getId().equals(currentUserId)) {
                throw new UnauthorizedAccessException("You are not authorized to verify payments for another customer's invoice");
            }
        }

        String paymentId = request.getRazorpayPaymentId();
        String orderId = request.getRazorpayOrderId();
        String signature = request.getRazorpaySignature();
        String paymentMethod = request.getPaymentMethod() != null ? request.getPaymentMethod() : "ONLINE";

        boolean isStaffRecording = !SecurityUtils.isCustomer();

        if (!isStaffRecording || "ONLINE".equalsIgnoreCase(paymentMethod)) {
            // Online / Gateway Payment Verification Rules
            if (paymentId == null || paymentId.trim().isEmpty()) {
                throw new BusinessRuleException("Payment transaction ID is required", "MISSING_PAYMENT_ID");
            }

            // Duplicate / Replay Check
            if (paymentRepository.existsByPaymentGatewayPaymentId(paymentId)) {
                throw new BusinessRuleException("Payment transaction has already been processed", "DUPLICATE_PAYMENT_TRANSACTION");
            }

            // Real Signature Verification when production/live keySecret is configured
            if (signature != null && !signature.trim().isEmpty() && keySecret != null && !keySecret.equals("mockKeySecret")) {
                verifyRazorpaySignature(orderId, paymentId, signature, keySecret);
            }
        }

        // Record Payment
        Payment payment = Payment.builder()
                .invoice(invoice)
                .paymentGatewayOrderId(orderId != null ? orderId : "OFFLINE_ORDER")
                .paymentGatewayPaymentId(paymentId != null ? paymentId : "pay_off_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12))
                .paymentGatewaySignature(signature)
                .amount(invoice.getTotalAmount())
                .paymentMethod(paymentMethod)
                .paymentStatus("COMPLETED")
                .verifiedAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        invoice.setPaymentStatus("PAID");
        invoiceRepository.save(invoice);

        // Update RepairJob status & Consume inventory stock
        RepairJob job = invoice.getRepairJob();
        if (job != null) {
            inventoryService.consumeReservedParts(job);

            if (job.getStatus() != RepairJobStatus.COMPLETED) {
                if (job.getStatus() == RepairJobStatus.READY_FOR_PICKUP) {
                    transitionValidator.validateTransition(job.getStatus(), RepairJobStatus.COMPLETED);
                    job.setStatus(RepairJobStatus.COMPLETED);
                } else {
                    job.setStatus(RepairJobStatus.COMPLETED);
                }
                job.setCompletedAt(LocalDateTime.now());
                job.setTotalActualCost(invoice.getTotalAmount());
                repairJobRepository.save(job);
            }
        }

        return true;
    }

    private void verifyRazorpaySignature(String orderId, String paymentId, String signature, String secret) {
        try {
            String data = orderId + "|" + paymentId;
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            String expectedSignature = HexFormat.of().formatHex(hash);

            if (!expectedSignature.equalsIgnoreCase(signature)) {
                throw new BusinessRuleException("Payment signature verification failed", "INVALID_PAYMENT_SIGNATURE");
            }
        } catch (BusinessRuleException e) {
            throw e;
        } catch (Exception e) {
            log.error("Payment signature verification error: {}", e.getMessage());
            throw new BusinessRuleException("Error verifying payment signature", "PAYMENT_VERIFICATION_ERROR");
        }
    }
}
