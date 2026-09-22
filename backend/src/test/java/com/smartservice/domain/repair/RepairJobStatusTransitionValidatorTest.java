package com.smartservice.domain.repair;

import com.smartservice.common.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RepairJobStatusTransitionValidatorTest {

    private RepairJobStatusTransitionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new RepairJobStatusTransitionValidator();
    }

    @Test
    @DisplayName("Valid transition from REQUESTED to ASSIGNED should pass")
    void testValidTransition() {
        assertDoesNotThrow(() -> validator.validateTransition(RepairJobStatus.REQUESTED, RepairJobStatus.ASSIGNED));
    }

    @Test
    @DisplayName("Invalid transition from COMPLETED to DIAGNOSING must throw BusinessRuleException")
    void testInvalidTransitionFromCompleted() {
        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> validator.validateTransition(RepairJobStatus.COMPLETED, RepairJobStatus.DIAGNOSING));

        assertEquals("INVALID_STATUS_TRANSITION", ex.getCode());
    }

    @Test
    @DisplayName("Same status transition should pass without error")
    void testSameStatusTransition() {
        assertDoesNotThrow(() -> validator.validateTransition(RepairJobStatus.IN_REPAIR, RepairJobStatus.IN_REPAIR));
    }
}
