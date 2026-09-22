package com.smartservice.domain.estimate;

import com.smartservice.common.exception.BusinessRuleException;
import com.smartservice.common.exception.ResourceNotFoundException;
import com.smartservice.common.exception.UnauthorizedAccessException;
import com.smartservice.common.util.SecurityUtils;
import com.smartservice.domain.estimate.dto.*;
import com.smartservice.domain.inventory.InventoryService;
import com.smartservice.domain.inventory.Part;
import com.smartservice.domain.inventory.PartRepository;
import com.smartservice.domain.repair.RepairJob;
import com.smartservice.domain.repair.RepairJobRepository;
import com.smartservice.domain.repair.RepairJobStatus;
import com.smartservice.domain.repair.RepairJobStatusTransitionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EstimateService {

    private final EstimateRepository estimateRepository;
    private final RepairJobRepository repairJobRepository;
    private final PartRepository partRepository;
    private final InventoryService inventoryService;
    private final RepairJobStatusTransitionValidator transitionValidator;

    @Transactional
    public EstimateDTO createEstimate(CreateEstimateRequest request) {
        RepairJob job = repairJobRepository.findById(request.getRepairJobId())
                .orElseThrow(() -> new ResourceNotFoundException("RepairJob", "id", request.getRepairJobId()));

        Estimate estimate = estimateRepository.findByRepairJobId(job.getId())
                .orElseGet(() -> Estimate.builder()
                        .estimateNumber("EST-" + (100000 + new Random().nextInt(900000)))
                        .repairJob(job)
                        .customer(job.getCustomer())
                        .status("DRAFT")
                        .items(new ArrayList<>())
                        .build());

        estimate.getItems().clear();

        BigDecimal partsSubtotal = BigDecimal.ZERO;
        if (request.getItems() != null) {
            for (var itemReq : request.getItems()) {
                Part part = null;
                String desc = itemReq.getItemDescription();
                BigDecimal unitPrice = itemReq.getUnitPrice() != null ? itemReq.getUnitPrice() : BigDecimal.ZERO;

                if (itemReq.getPartId() != null) {
                    part = partRepository.findById(itemReq.getPartId()).orElse(null);
                    if (part != null) {
                        if (desc == null || desc.isBlank()) desc = part.getName();
                        if (unitPrice.compareTo(BigDecimal.ZERO) == 0) unitPrice = part.getSellingPrice();
                    }
                }

                int qty = itemReq.getQuantity() != null ? itemReq.getQuantity() : 1;
                BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));
                partsSubtotal = partsSubtotal.add(lineTotal);

                EstimateItem item = EstimateItem.builder()
                        .estimate(estimate)
                        .part(part)
                        .itemDescription(desc != null ? desc : "Repair Part / Service")
                        .quantity(qty)
                        .unitPrice(unitPrice)
                        .totalPrice(lineTotal)
                        .build();

                estimate.getItems().add(item);
            }
        }

        BigDecimal laborCost = request.getLaborCost() != null ? request.getLaborCost() : job.getLaborCost();
        if (laborCost == null) laborCost = BigDecimal.ZERO;

        BigDecimal taxRate = request.getTaxRatePercentage() != null ? request.getTaxRatePercentage() : new BigDecimal("18.00");
        BigDecimal subtotal = partsSubtotal.add(laborCost);
        BigDecimal taxAmount = subtotal.multiply(taxRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotal.add(taxAmount);

        estimate.setTotalPartsCost(partsSubtotal);
        estimate.setLaborCost(laborCost);
        estimate.setTaxAmount(taxAmount);
        estimate.setTotalAmount(totalAmount);
        estimate.setStatus("SENT");

        estimate = estimateRepository.save(estimate);

        // Update Job Status
        transitionValidator.validateTransition(job.getStatus(), RepairJobStatus.ESTIMATE_CREATED);
        job.setStatus(RepairJobStatus.ESTIMATE_CREATED);

        transitionValidator.validateTransition(RepairJobStatus.ESTIMATE_CREATED, RepairJobStatus.WAITING_FOR_APPROVAL);
        job.setStatus(RepairJobStatus.WAITING_FOR_APPROVAL);
        job.setTotalEstimatedCost(totalAmount);
        repairJobRepository.save(job);

        return mapToDTO(estimate);
    }

    @Transactional
    public EstimateDTO processApproval(Long estimateId, ApproveRejectEstimateRequest request) {
        Estimate estimate = estimateRepository.findById(estimateId)
                .orElseThrow(() -> new ResourceNotFoundException("Estimate", "id", estimateId));

        RepairJob job = estimate.getRepairJob();

        if (SecurityUtils.isCustomer()) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (job.getCustomer().getUser() == null || !job.getCustomer().getUser().getId().equals(currentUserId)) {
                throw new UnauthorizedAccessException("You can only approve or reject estimates for your own repairs");
            }
        }

        if (!"DRAFT".equals(estimate.getStatus()) && !"SENT".equals(estimate.getStatus())) {
            throw new BusinessRuleException("Estimate has already been processed: " + estimate.getStatus(), "ESTIMATE_ALREADY_PROCESSED");
        }

        if (request.isApproved()) {
            estimate.setStatus("APPROVED");
            estimate.setApprovedAt(LocalDateTime.now());

            transitionValidator.validateTransition(job.getStatus(), RepairJobStatus.APPROVED);
            job.setStatus(RepairJobStatus.APPROVED);

            // Reserve stock parts for this job
            for (EstimateItem item : estimate.getItems()) {
                if (item.getPart() != null) {
                    inventoryService.reservePart(item.getPart(), job, item.getQuantity());
                }
            }
        } else {
            estimate.setStatus("REJECTED");
            estimate.setRejectedAt(LocalDateTime.now());
            estimate.setRejectionReason(request.getRejectionReason());

            transitionValidator.validateTransition(job.getStatus(), RepairJobStatus.REJECTED);
            job.setStatus(RepairJobStatus.REJECTED);
        }

        repairJobRepository.save(job);
        estimate = estimateRepository.save(estimate);

        return mapToDTO(estimate);
    }

    @Transactional(readOnly = true)
    public EstimateDTO getEstimateByJobId(Long jobId) {
        Estimate estimate = estimateRepository.findByRepairJobId(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Estimate not found for job ID: " + jobId));

        if (SecurityUtils.isCustomer()) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (estimate.getCustomer().getUser() == null || !estimate.getCustomer().getUser().getId().equals(currentUserId)) {
                throw new UnauthorizedAccessException("Access denied to estimate details");
            }
        }

        return mapToDTO(estimate);
    }

    public EstimateDTO mapToDTO(Estimate estimate) {
        List<EstimateItemDTO> itemDTOs = estimate.getItems() != null ? estimate.getItems().stream()
                .map(item -> EstimateItemDTO.builder()
                        .id(item.getId())
                        .partId(item.getPart() != null ? item.getPart().getId() : null)
                        .itemDescription(item.getItemDescription())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .build())
                .toList() : List.of();

        return EstimateDTO.builder()
                .id(estimate.getId())
                .estimateNumber(estimate.getEstimateNumber())
                .repairJobId(estimate.getRepairJob().getId())
                .jobNumber(estimate.getRepairJob().getJobNumber())
                .customerId(estimate.getCustomer().getId())
                .customerName(estimate.getCustomer().getName())
                .status(estimate.getStatus())
                .totalPartsCost(estimate.getTotalPartsCost())
                .laborCost(estimate.getLaborCost())
                .taxAmount(estimate.getTaxAmount())
                .totalAmount(estimate.getTotalAmount())
                .rejectionReason(estimate.getRejectionReason())
                .approvedAt(estimate.getApprovedAt())
                .rejectedAt(estimate.getRejectedAt())
                .createdAt(estimate.getCreatedAt())
                .items(itemDTOs)
                .build();
    }
}
