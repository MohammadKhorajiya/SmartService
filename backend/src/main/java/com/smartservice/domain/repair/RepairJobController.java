package com.smartservice.domain.repair;

import com.smartservice.common.dto.ApiResponse;
import com.smartservice.common.dto.PageResponse;
import com.smartservice.domain.repair.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/repair-jobs")
@RequiredArgsConstructor
@Tag(name = "Repair Jobs", description = "Repair job management, status state machine, and technician assignment")
public class RepairJobController {

    private final RepairJobService repairJobService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @Operation(summary = "Create a new repair job")
    public ResponseEntity<ApiResponse<RepairJobDTO>> createJob(@Valid @RequestBody CreateRepairJobRequest request) {
        RepairJobDTO dto = repairJobService.createJob(request);
        return ResponseEntity.ok(ApiResponse.success(dto, "Repair job created successfully"));
    }

    @PostMapping("/{id}/assign-technician")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Assign a technician to a repair job")
    public ResponseEntity<ApiResponse<RepairJobDTO>> assignTechnician(
            @PathVariable Long id,
            @Valid @RequestBody AssignTechnicianRequest request) {
        RepairJobDTO dto = repairJobService.assignTechnician(id, request);
        return ResponseEntity.ok(ApiResponse.success(dto, "Technician assigned successfully"));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update repair job status (enforces state machine rules)")
    public ResponseEntity<ApiResponse<RepairJobDTO>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobStatusRequest request) {
        RepairJobDTO dto = repairJobService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(dto, "Repair job status updated"));
    }

    @GetMapping
    @Operation(summary = "Filter and paginate repair jobs")
    public ResponseEntity<ApiResponse<PageResponse<RepairJobDTO>>> filterJobs(
            @RequestParam(required = false) RepairJobStatus status,
            @RequestParam(required = false) Long technicianId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String query,
            @PageableDefault(size = 10) Pageable pageable) {
        PageResponse<RepairJobDTO> page = repairJobService.filterJobs(status, technicianId, customerId, query, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get repair job details by ID")
    public ResponseEntity<ApiResponse<RepairJobDTO>> getJobById(@PathVariable Long id) {
        RepairJobDTO dto = repairJobService.getJobById(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Get repair job status audit timeline")
    public ResponseEntity<ApiResponse<List<RepairJobStatusHistoryDTO>>> getJobHistory(@PathVariable Long id) {
        List<RepairJobStatusHistoryDTO> history = repairJobService.getJobStatusHistory(id);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}
