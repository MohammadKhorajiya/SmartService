package com.smartservice.domain.device;

import com.smartservice.common.dto.ApiResponse;
import com.smartservice.common.dto.PageResponse;
import com.smartservice.domain.device.dto.CreateDeviceRequest;
import com.smartservice.domain.device.dto.DeviceDTO;
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
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@Tag(name = "Device Management", description = "Endpoints for registering & managing customer devices")
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "Register a new device")
    public ResponseEntity<ApiResponse<DeviceDTO>> createDevice(@Valid @RequestBody CreateDeviceRequest request) {
        DeviceDTO deviceDTO = deviceService.createDevice(request);
        return ResponseEntity.ok(ApiResponse.success(deviceDTO, "Device registered successfully"));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "Get list of devices owned by a customer")
    public ResponseEntity<ApiResponse<List<DeviceDTO>>> getCustomerDevicesList(@PathVariable Long customerId) {
        List<DeviceDTO> devices = deviceService.getCustomerDevicesList(customerId);
        return ResponseEntity.ok(ApiResponse.success(devices));
    }

    @GetMapping("/customer/{customerId}/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "Get paginated devices for a customer")
    public ResponseEntity<ApiResponse<PageResponse<DeviceDTO>>> getCustomerDevices(
            @PathVariable Long customerId,
            @PageableDefault(size = 10) Pageable pageable) {
        PageResponse<DeviceDTO> page = deviceService.getCustomerDevices(customerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "Get device details by ID")
    public ResponseEntity<ApiResponse<DeviceDTO>> getDeviceById(@PathVariable Long id) {
        DeviceDTO device = deviceService.getDeviceById(id);
        return ResponseEntity.ok(ApiResponse.success(device));
    }
}
