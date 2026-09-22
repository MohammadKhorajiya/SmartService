package com.smartservice.domain.repair;

import com.smartservice.common.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RepairJobStatusTransitionValidator {

    private static final Map<RepairJobStatus, Set<RepairJobStatus>> VALID_TRANSITIONS = new EnumMap<>(RepairJobStatus.class);

    static {
        VALID_TRANSITIONS.put(RepairJobStatus.REQUESTED, Set.of(RepairJobStatus.ASSIGNED, RepairJobStatus.REJECTED, RepairJobStatus.CANCELLED));
        VALID_TRANSITIONS.put(RepairJobStatus.ASSIGNED, Set.of(RepairJobStatus.DIAGNOSING, RepairJobStatus.CANCELLED));
        VALID_TRANSITIONS.put(RepairJobStatus.DIAGNOSING, Set.of(RepairJobStatus.ESTIMATE_CREATED, RepairJobStatus.CANCELLED));
        VALID_TRANSITIONS.put(RepairJobStatus.ESTIMATE_CREATED, Set.of(RepairJobStatus.WAITING_FOR_APPROVAL, RepairJobStatus.CANCELLED));
        VALID_TRANSITIONS.put(RepairJobStatus.WAITING_FOR_APPROVAL, Set.of(RepairJobStatus.APPROVED, RepairJobStatus.REJECTED, RepairJobStatus.CANCELLED));
        VALID_TRANSITIONS.put(RepairJobStatus.APPROVED, Set.of(RepairJobStatus.WAITING_FOR_PARTS, RepairJobStatus.IN_REPAIR, RepairJobStatus.CANCELLED));
        VALID_TRANSITIONS.put(RepairJobStatus.REJECTED, Set.of(RepairJobStatus.CANCELLED, RepairJobStatus.DIAGNOSING));
        VALID_TRANSITIONS.put(RepairJobStatus.WAITING_FOR_PARTS, Set.of(RepairJobStatus.IN_REPAIR, RepairJobStatus.CANCELLED));
        VALID_TRANSITIONS.put(RepairJobStatus.IN_REPAIR, Set.of(RepairJobStatus.READY_FOR_QC, RepairJobStatus.WAITING_FOR_PARTS, RepairJobStatus.CANCELLED));
        VALID_TRANSITIONS.put(RepairJobStatus.READY_FOR_QC, Set.of(RepairJobStatus.READY_FOR_PICKUP, RepairJobStatus.QC_FAILED, RepairJobStatus.CANCELLED));
        VALID_TRANSITIONS.put(RepairJobStatus.QC_FAILED, Set.of(RepairJobStatus.IN_REPAIR, RepairJobStatus.CANCELLED));
        VALID_TRANSITIONS.put(RepairJobStatus.READY_FOR_PICKUP, Set.of(RepairJobStatus.COMPLETED, RepairJobStatus.CANCELLED));
        VALID_TRANSITIONS.put(RepairJobStatus.COMPLETED, Collections.emptySet()); // Terminal state
        VALID_TRANSITIONS.put(RepairJobStatus.CANCELLED, Collections.emptySet()); // Terminal state
    }

    public void validateTransition(RepairJobStatus currentStatus, RepairJobStatus newStatus) {
        if (currentStatus == newStatus) {
            return;
        }

        Set<RepairJobStatus> allowed = VALID_TRANSITIONS.getOrDefault(currentStatus, Collections.emptySet());
        if (!allowed.contains(newStatus)) {
            throw new BusinessRuleException(
                    String.format("Invalid status transition from '%s' to '%s'. Allowed transitions: %s",
                            currentStatus, newStatus, allowed),
                    "INVALID_STATUS_TRANSITION"
            );
        }
    }
}
