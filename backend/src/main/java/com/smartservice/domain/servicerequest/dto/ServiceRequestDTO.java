package com.smartservice.domain.servicerequest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRequestDTO {

    private Long id;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private Long deviceId;
    private String deviceBrand;
    private String deviceModel;
    private String problemTitle;
    private String description;
    private String priority;
    private String status;
    private LocalDateTime createdAt;
}
