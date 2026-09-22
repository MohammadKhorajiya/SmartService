package com.smartservice.domain.payment;

import com.smartservice.common.exception.BusinessRuleException;
import com.smartservice.common.exception.ResourceNotFoundException;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

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

    @Transactional
    public PaymentOrderResponse createPaymentOrder(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", invoiceId));

        if ("PAID".equals(invoice.getPaymentStatus())) {
            throw new BusinessRuleException("Invoice is already fully paid", "INVOICE_ALREADY_PAID");
        }

        String mockOrderId = "order_" + UUID.randomUUID().toString().substring(0, 16);

        return PaymentOrderResponse.builder()
                .orderId(mockOrderId)
                .invoiceId(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .amount(invoice.getTotalAmount())
                .currency("INR")
                .keyId(keyId)
                .build();
    }

    @Transactional
    public boolean verifyAndRecordPayment(VerifyPaymentRequest request) {
        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", request.getInvoiceId()));

        if ("PAID".equals(invoice.getPaymentStatus())) {
            return true; // Already processed
        }

        // Record payment record
        Payment payment = Payment.builder()
                .invoice(invoice)
                .paymentGatewayOrderId(request.getRazorpayOrderId() != null ? request.getRazorpayOrderId() : "MOCK_ORDER")
                .paymentGatewayPaymentId(request.getRazorpayPaymentId() != null ? request.getRazorpayPaymentId() : "pay_" + UUID.randomUUID().toString().substring(0, 12))
                .paymentGatewaySignature(request.getRazorpaySignature())
                .amount(invoice.getTotalAmount())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "ONLINE")
                .paymentStatus("COMPLETED")
                .verifiedAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        invoice.setPaymentStatus("PAID");
        invoiceRepository.save(invoice);

        // Update RepairJob status & Consume inventory stock
        RepairJob job = invoice.getRepairJob();
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

        return true;
    }
}
