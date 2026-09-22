package com.smartservice.domain.repair;

import com.smartservice.domain.customer.Customer;
import com.smartservice.domain.device.Device;
import com.smartservice.domain.servicerequest.ServiceRequest;
import com.smartservice.domain.technician.Technician;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "repair_jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepairJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_number", nullable = false, unique = true)
    private String jobNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_request_id")
    private ServiceRequest serviceRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id")
    private Technician technician;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepairJobStatus status;

    @Column(nullable = false)
    private String priority;

    @Column(name = "labor_cost", precision = 10, scale = 2)
    private BigDecimal laborCost;

    @Column(name = "total_estimated_cost", precision = 10, scale = 2)
    private BigDecimal totalEstimatedCost;

    @Column(name = "total_actual_cost", precision = 10, scale = 2)
    private BigDecimal totalActualCost;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (laborCost == null) laborCost = BigDecimal.ZERO;
        if (totalEstimatedCost == null) totalEstimatedCost = BigDecimal.ZERO;
        if (totalActualCost == null) totalActualCost = BigDecimal.ZERO;
        if (status == null) status = RepairJobStatus.REQUESTED;
        if (priority == null) priority = "MEDIUM";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
