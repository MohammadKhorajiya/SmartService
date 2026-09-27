package com.smartservice.domain.servicerequest;

import com.smartservice.common.dto.ApiResponse;
import com.smartservice.common.dto.PageResponse;
import com.smartservice.domain.servicerequest.dto.CreateServiceRequest;
import com.smartservice.domain.servicerequest.dto.ServiceRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/service-requests")
@RequiredArgsConstructor
@Tag(name = "Service Requests", description = "Endpoints for customer service request submission and management")
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "Submit a new service request")
    public ResponseEntity<ApiResponse<ServiceRequestDTO>> createServiceRequest(@Valid @RequestBody CreateServiceRequest request) {
        ServiceRequestDTO dto = serviceRequestService.createRequest(request);
        return ResponseEntity.ok(ApiResponse.success(dto, "Service request submitted successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "Get paginated service requests with optional filters")
    public ResponseEntity<ApiResponse<PageResponse<ServiceRequestDTO>>> getServiceRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long customerId,
            @PageableDefault(size = 10) Pageable pageable) {
        PageResponse<ServiceRequestDTO> page = serviceRequestService.getServiceRequests(status, customerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "Get service request details by ID")
    public ResponseEntity<ApiResponse<ServiceRequestDTO>> getServiceRequestById(@PathVariable Long id) {
        ServiceRequestDTO dto = serviceRequestService.getServiceRequestById(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
