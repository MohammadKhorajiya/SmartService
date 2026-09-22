package com.smartservice.domain.estimate.dto;

import lombok.Data;

@Data
public class ApproveRejectEstimateRequest {
    private boolean approved;
    private String rejectionReason;
}
