package com.smartservice.domain.invoice;

import com.smartservice.common.dto.PageResponse;
import com.smartservice.common.exception.ResourceNotFoundException;
import com.smartservice.common.exception.UnauthorizedAccessException;
import com.smartservice.common.util.SecurityUtils;
import com.smartservice.domain.customer.Customer;
import com.smartservice.domain.customer.CustomerRepository;
import com.smartservice.domain.estimate.Estimate;
import com.smartservice.domain.estimate.EstimateItem;
import com.smartservice.domain.estimate.EstimateRepository;
import com.smartservice.domain.invoice.dto.InvoiceDTO;
import com.smartservice.domain.invoice.dto.InvoiceItemDTO;
import com.smartservice.domain.repair.RepairJob;
import com.smartservice.domain.repair.RepairJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final RepairJobRepository repairJobRepository;
    private final EstimateRepository estimateRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public InvoiceDTO generateInvoiceForJob(Long jobId) {
        RepairJob job = repairJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("RepairJob", "id", jobId));

        return invoiceRepository.findByRepairJobId(jobId)
                .map(this::mapToDTO)
                .orElseGet(() -> createInvoiceFromJob(job));
    }

    private InvoiceDTO createInvoiceFromJob(RepairJob job) {
        Estimate estimate = estimateRepository.findByRepairJobId(job.getId()).orElse(null);

        Invoice invoice = Invoice.builder()
                .invoiceNumber("INV-" + (100000 + new Random().nextInt(900000)))
                .repairJob(job)
                .customer(job.getCustomer())
                .paymentStatus("PENDING")
                .items(new ArrayList<>())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;

        if (estimate != null && estimate.getItems() != null) {
            for (EstimateItem estItem : estimate.getItems()) {
                subtotal = subtotal.add(estItem.getTotalPrice());
                InvoiceItem invItem = InvoiceItem.builder()
                        .invoice(invoice)
                        .description(estItem.getItemDescription())
                        .quantity(estItem.getQuantity())
                        .unitPrice(estItem.getUnitPrice())
                        .totalPrice(estItem.getTotalPrice())
                        .itemType("PART")
                        .build();
                invoice.getItems().add(invItem);
            }
        }

        BigDecimal labor = job.getLaborCost() != null ? job.getLaborCost() : (estimate != null ? estimate.getLaborCost() : BigDecimal.ZERO);
        if (labor.compareTo(BigDecimal.ZERO) > 0) {
            subtotal = subtotal.add(labor);
            InvoiceItem laborItem = InvoiceItem.builder()
                    .invoice(invoice)
                    .description("Technician Labor & Service Charge")
                    .quantity(1)
                    .unitPrice(labor)
                    .totalPrice(labor)
                    .itemType("LABOR")
                    .build();
            invoice.getItems().add(laborItem);
        }

        BigDecimal taxAmount = estimate != null ? estimate.getTaxAmount() : subtotal.multiply(new BigDecimal("0.18"));
        BigDecimal totalAmount = subtotal.add(taxAmount);

        invoice.setSubtotal(subtotal);
        invoice.setTaxAmount(taxAmount);
        invoice.setDiscountAmount(BigDecimal.ZERO);
        invoice.setTotalAmount(totalAmount);

        invoice = invoiceRepository.save(invoice);
        return mapToDTO(invoice);
    }

    @Transactional(readOnly = true)
    public PageResponse<InvoiceDTO> getInvoices(String status, Long customerId, Pageable pageable) {
        final Long targetCustomerId;
        if (SecurityUtils.isCustomer()) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            Customer customer = customerRepository.findByUserId(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for logged in user"));
            targetCustomerId = customer.getId();
        } else {
            targetCustomerId = customerId;
        }

        Page<Invoice> page = invoiceRepository.filterInvoices(status, targetCustomerId, pageable);
        return PageResponse.from(page.map(this::mapToDTO));
    }

    @Transactional(readOnly = true)
    public InvoiceDTO getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));

        if (SecurityUtils.isCustomer()) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (invoice.getCustomer().getUser() == null || !invoice.getCustomer().getUser().getId().equals(currentUserId)) {
                throw new UnauthorizedAccessException("Access denied to invoice");
            }
        }

        return mapToDTO(invoice);
    }

    public InvoiceDTO mapToDTO(Invoice invoice) {
        List<InvoiceItemDTO> items = invoice.getItems() != null ? invoice.getItems().stream()
                .map(item -> InvoiceItemDTO.builder()
                        .id(item.getId())
                        .description(item.getDescription())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .itemType(item.getItemType())
                        .build())
                .toList() : List.of();

        return InvoiceDTO.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .repairJobId(invoice.getRepairJob().getId())
                .jobNumber(invoice.getRepairJob().getJobNumber())
                .customerId(invoice.getCustomer().getId())
                .customerName(invoice.getCustomer().getName())
                .customerEmail(invoice.getCustomer().getEmail())
                .customerPhone(invoice.getCustomer().getPhone())
                .deviceBrand(invoice.getRepairJob().getDevice().getBrand())
                .deviceModel(invoice.getRepairJob().getDevice().getModel())
                .subtotal(invoice.getSubtotal())
                .taxAmount(invoice.getTaxAmount())
                .discountAmount(invoice.getDiscountAmount())
                .totalAmount(invoice.getTotalAmount())
                .paymentStatus(invoice.getPaymentStatus())
                .createdAt(invoice.getCreatedAt())
                .items(items)
                .build();
    }
}
