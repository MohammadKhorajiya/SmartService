package com.smartservice.domain.estimate;

import com.smartservice.common.dto.ApiResponse;
import com.smartservice.domain.estimate.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/estimates")
@RequiredArgsConstructor
@Tag(name = "Repair Estimates", description = "Endpoints for generating repair cost estimates and customer approval")
public class EstimateController {

    private final EstimateService estimateService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'TECHNICIAN')")
    @Operation(summary = "Generate or update a repair cost estimate")
    public ResponseEntity<ApiResponse<EstimateDTO>> createEstimate(@Valid @RequestBody CreateEstimateRequest request) {
        EstimateDTO dto = estimateService.createEstimate(request);
        return ResponseEntity.ok(ApiResponse.success(dto, "Estimate generated successfully"));
    }

    @PostMapping("/{id}/approval")
    @Operation(summary = "Approve or reject a repair estimate (Customer or Staff)")
    public ResponseEntity<ApiResponse<EstimateDTO>> processApproval(
            @PathVariable Long id,
            @RequestBody ApproveRejectEstimateRequest request) {
        EstimateDTO dto = estimateService.processApproval(id, request);
        String msg = request.isApproved() ? "Estimate approved and parts reserved" : "Estimate rejected";
        return ResponseEntity.ok(ApiResponse.success(dto, msg));
    }

    @GetMapping("/job/{jobId}")
    @Operation(summary = "Get estimate by repair job ID")
    public ResponseEntity<ApiResponse<EstimateDTO>> getEstimateByJobId(@PathVariable Long jobId) {
        EstimateDTO dto = estimateService.getEstimateByJobId(jobId);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
