package com.smartservice.domain.device.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeviceDTO {

    private Long id;
    private Long customerId;
    private String customerName;
    private String brand;
    private String model;
    private String serialNumber;
    private String imei;
    private String deviceType;
    private LocalDateTime createdAt;
}
