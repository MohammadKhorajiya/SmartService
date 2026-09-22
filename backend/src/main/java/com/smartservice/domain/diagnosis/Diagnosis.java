package com.smartservice.domain.diagnosis;

import com.smartservice.domain.repair.RepairJob;
import com.smartservice.domain.technician.Technician;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "diagnoses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Diagnosis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repair_job_id", nullable = false, unique = true)
    private RepairJob repairJob;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = false)
    private Technician technician;

    @Column(columnDefinition = "TEXT")
    private String symptoms;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String findings;

    @Column(name = "recommended_repair", columnDefinition = "TEXT")
    private String recommendedRepair;

    @Column(name = "estimated_labor_cost", precision = 10, scale = 2)
    private BigDecimal estimatedLaborCost;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (estimatedLaborCost == null) estimatedLaborCost = BigDecimal.ZERO;
    }
}
