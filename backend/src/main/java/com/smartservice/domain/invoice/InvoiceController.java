package com.smartservice.domain.invoice;

import com.smartservice.common.dto.ApiResponse;
import com.smartservice.common.dto.PageResponse;
import com.smartservice.domain.invoice.dto.InvoiceDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices & Billing", description = "Endpoints for invoice generation, retrieval, and billing status")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping("/job/{jobId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @Operation(summary = "Generate invoice for a repair job")
    public ResponseEntity<ApiResponse<InvoiceDTO>> generateInvoice(@PathVariable Long jobId) {
        InvoiceDTO dto = invoiceService.generateInvoiceForJob(jobId);
        return ResponseEntity.ok(ApiResponse.success(dto, "Invoice generated successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "Get paginated list of invoices")
    public ResponseEntity<ApiResponse<PageResponse<InvoiceDTO>>> getInvoices(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long customerId,
            @PageableDefault(size = 10) Pageable pageable) {
        PageResponse<InvoiceDTO> page = invoiceService.getInvoices(status, customerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "Get invoice by ID")
    public ResponseEntity<ApiResponse<InvoiceDTO>> getInvoiceById(@PathVariable Long id) {
        InvoiceDTO dto = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
