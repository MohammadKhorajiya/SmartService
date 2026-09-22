package com.smartservice.domain.payment;

import com.smartservice.domain.invoice.Invoice;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "payment_gateway_order_id")
    private String paymentGatewayOrderId;

    @Column(name = "payment_gateway_payment_id")
    private String paymentGatewayPaymentId;

    @Column(name = "payment_gateway_signature")
    private String paymentGatewaySignature;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod; // ONLINE, CASH, CARD, UPI

    @Column(name = "payment_status", nullable = false)
    private String paymentStatus; // COMPLETED, FAILED, PENDING

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (paymentMethod == null) paymentMethod = "ONLINE";
        if (paymentStatus == null) paymentStatus = "COMPLETED";
    }
}
