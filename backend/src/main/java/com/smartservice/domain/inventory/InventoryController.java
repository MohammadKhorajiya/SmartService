package com.smartservice.domain.inventory;

import com.smartservice.common.dto.ApiResponse;
import com.smartservice.common.dto.PageResponse;
import com.smartservice.domain.inventory.dto.*;
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
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory & Spare Parts", description = "Endpoints for inventory control, stock adjustments, and parts management")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/parts")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create a new spare part SKU")
    public ResponseEntity<ApiResponse<PartDTO>> createPart(@Valid @RequestBody CreatePartRequest request) {
        PartDTO dto = inventoryService.createPart(request);
        return ResponseEntity.ok(ApiResponse.success(dto, "Part created successfully"));
    }

    @PostMapping("/adjust-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @Operation(summary = "Adjust stock level with transaction logging")
    public ResponseEntity<ApiResponse<PartDTO>> adjustStock(@Valid @RequestBody StockAdjustmentRequest request) {
        PartDTO dto = inventoryService.adjustStock(request);
        return ResponseEntity.ok(ApiResponse.success(dto, "Stock updated successfully"));
    }

    @GetMapping("/parts")
    @Operation(summary = "Search and filter inventory parts")
    public ResponseEntity<ApiResponse<PageResponse<PartDTO>>> searchParts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String query,
            @PageableDefault(size = 10) Pageable pageable) {
        PageResponse<PartDTO> page = inventoryService.searchParts(category, active, query, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/parts/{id}")
    @Operation(summary = "Get spare part details by ID")
    public ResponseEntity<ApiResponse<PartDTO>> getPartById(@PathVariable Long id) {
        PartDTO dto = inventoryService.getPartById(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
