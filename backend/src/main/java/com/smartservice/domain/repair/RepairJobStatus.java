package com.smartservice.domain.repair;

public enum RepairJobStatus {
    REQUESTED,
    ASSIGNED,
    DIAGNOSING,
    ESTIMATE_CREATED,
    WAITING_FOR_APPROVAL,
    APPROVED,
    REJECTED,
    WAITING_FOR_PARTS,
    IN_REPAIR,
    READY_FOR_QC,
    QC_FAILED,
    READY_FOR_PICKUP,
    COMPLETED,
    CANCELLED
}
