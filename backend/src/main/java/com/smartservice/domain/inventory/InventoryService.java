package com.smartservice.domain.inventory;

import com.smartservice.common.dto.PageResponse;
import com.smartservice.common.exception.BusinessRuleException;
import com.smartservice.common.exception.ResourceNotFoundException;
import com.smartservice.common.util.SecurityUtils;
import com.smartservice.domain.inventory.dto.*;
import com.smartservice.domain.repair.RepairJob;
import com.smartservice.domain.user.User;
import com.smartservice.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final PartRepository partRepository;
    private final StockTransactionRepository transactionRepository;
    private final InventoryReservationRepository reservationRepository;
    private final UserRepository userRepository;

    @Transactional
    public PartDTO createPart(CreatePartRequest request) {
        if (partRepository.findBySku(request.getSku()).isPresent()) {
            throw new BusinessRuleException("SKU already exists: " + request.getSku(), "DUPLICATE_SKU");
        }

        Part part = Part.builder()
                .sku(request.getSku())
                .name(request.getName())
                .category(request.getCategory())
                .compatibleDevice(request.getCompatibleDevice())
                .purchasePrice(request.getPurchasePrice())
                .sellingPrice(request.getSellingPrice())
                .quantityInStock(request.getQuantityInStock())
                .reservedQuantity(0)
                .reorderLevel(request.getReorderLevel() != null ? request.getReorderLevel() : 5)
                .active(true)
                .build();

        part = partRepository.save(part);

        if (request.getQuantityInStock() > 0) {
            recordTransaction(part, "PURCHASE", request.getQuantityInStock(), null, "INITIAL_STOCK", "Initial stock setup");
        }

        return mapToDTO(part);
    }

    @Transactional
    public PartDTO adjustStock(StockAdjustmentRequest request) {
        // Pessimistic DB write lock to guarantee safety under concurrent transactions
        Part part = partRepository.findByIdForUpdate(request.getPartId())
                .orElseThrow(() -> new ResourceNotFoundException("Part", "id", request.getPartId()));

        int newStock = part.getQuantityInStock() + request.getQuantity();
        if (newStock < 0) {
            throw new BusinessRuleException("Stock level cannot be negative. Current stock: " + part.getQuantityInStock(), "NEGATIVE_STOCK");
        }

        part.setQuantityInStock(newStock);
        part = partRepository.save(part);

        recordTransaction(part, request.getTransactionType(), request.getQuantity(), null, "MANUAL", request.getNotes());

        return mapToDTO(part);
    }

    @Transactional
    public InventoryReservation reservePart(Part part, RepairJob job, int quantity) {
        // Pessimistic write lock for concurrency control
        Part lockedPart = partRepository.findByIdForUpdate(part.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Part", "id", part.getId()));

        if (lockedPart.getAvailableQuantity() < quantity) {
            throw new BusinessRuleException(
                    String.format("Insufficient available stock for part '%s'. Required: %d, Available: %d",
                            lockedPart.getName(), quantity, lockedPart.getAvailableQuantity()),
                    "INSUFFICIENT_STOCK"
            );
        }

        lockedPart.setReservedQuantity(lockedPart.getReservedQuantity() + quantity);
        partRepository.save(lockedPart);

        Optional<InventoryReservation> existingRes = reservationRepository.findByPartIdAndRepairJobIdAndStatus(
                lockedPart.getId(), job.getId(), "RESERVED");

        InventoryReservation reservation;
        if (existingRes.isPresent()) {
            reservation = existingRes.get();
            reservation.setQuantity(reservation.getQuantity() + quantity);
        } else {
            reservation = InventoryReservation.builder()
                    .part(lockedPart)
                    .repairJob(job)
                    .quantity(quantity)
                    .status("RESERVED")
                    .build();
        }

        reservation = reservationRepository.save(reservation);
        recordTransaction(lockedPart, "RESERVATION", quantity, String.valueOf(job.getId()), "REPAIR_JOB", "Reserved for Job #" + job.getJobNumber());

        return reservation;
    }

    @Transactional
    public void consumeReservedParts(RepairJob job) {
        var reservations = reservationRepository.findByRepairJobIdAndStatus(job.getId(), "RESERVED");
        for (var res : reservations) {
            Part part = partRepository.findByIdForUpdate(res.getPart().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Part not found"));

            part.setReservedQuantity(Math.max(0, part.getReservedQuantity() - res.getQuantity()));
            part.setQuantityInStock(Math.max(0, part.getQuantityInStock() - res.getQuantity()));
            partRepository.save(part);

            res.setStatus("CONSUMED");
            reservationRepository.save(res);

            recordTransaction(part, "USED", -res.getQuantity(), String.valueOf(job.getId()), "REPAIR_JOB", "Consumed in completed Job #" + job.getJobNumber());
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<PartDTO> searchParts(String category, Boolean active, String query, Pageable pageable) {
        Page<Part> page = partRepository.searchParts(category, active, query, pageable);
        return PageResponse.from(page.map(this::mapToDTO));
    }

    @Transactional(readOnly = true)
    public PartDTO getPartById(Long id) {
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Part", "id", id));
        return mapToDTO(part);
    }

    private void recordTransaction(Part part, String type, int quantity, String refId, String refType, String notes) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User user = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;

        StockTransaction tx = StockTransaction.builder()
                .part(part)
                .transactionType(type)
                .quantity(quantity)
                .referenceId(refId)
                .referenceType(refType)
                .notes(notes)
                .createdByUser(user)
                .build();

        transactionRepository.save(tx);
    }

    public PartDTO mapToDTO(Part part) {
        return PartDTO.builder()
                .id(part.getId())
                .sku(part.getSku())
                .name(part.getName())
                .category(part.getCategory())
                .compatibleDevice(part.getCompatibleDevice())
                .purchasePrice(part.getPurchasePrice())
                .sellingPrice(part.getSellingPrice())
                .quantityInStock(part.getQuantityInStock())
                .reservedQuantity(part.getReservedQuantity())
                .availableQuantity(part.getAvailableQuantity())
                .reorderLevel(part.getReorderLevel())
                .active(part.isActive())
                .createdAt(part.getCreatedAt())
                .build();
    }
}
