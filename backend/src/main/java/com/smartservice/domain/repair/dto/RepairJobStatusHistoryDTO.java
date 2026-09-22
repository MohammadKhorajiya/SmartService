package com.smartservice.domain.repair.dto;

import com.smartservice.domain.repair.RepairJobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RepairJobStatusHistoryDTO {

    private Long id;
    private Long repairJobId;
    private RepairJobStatus oldStatus;
    private RepairJobStatus newStatus;
    private Long changedByUserId;
    private String changedByUserName;
    private String remarks;
    private LocalDateTime createdAt;
}
