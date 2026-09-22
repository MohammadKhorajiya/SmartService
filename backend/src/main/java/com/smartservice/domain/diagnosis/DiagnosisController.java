package com.smartservice.domain.diagnosis;

import com.smartservice.common.dto.ApiResponse;
import com.smartservice.domain.diagnosis.dto.CreateDiagnosisRequest;
import com.smartservice.domain.diagnosis.dto.DiagnosisDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/diagnoses")
@RequiredArgsConstructor
@Tag(name = "Diagnosis", description = "Endpoints for technician diagnosis & findings")
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Submit device diagnosis findings")
    public ResponseEntity<ApiResponse<DiagnosisDTO>> addDiagnosis(@Valid @RequestBody CreateDiagnosisRequest request) {
        DiagnosisDTO dto = diagnosisService.addDiagnosis(request);
        return ResponseEntity.ok(ApiResponse.success(dto, "Diagnosis recorded successfully"));
    }

    @GetMapping("/job/{jobId}")
    @Operation(summary = "Get diagnosis for a repair job")
    public ResponseEntity<ApiResponse<DiagnosisDTO>> getDiagnosisByJobId(@PathVariable Long jobId) {
        DiagnosisDTO dto = diagnosisService.getDiagnosisByJobId(jobId);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
