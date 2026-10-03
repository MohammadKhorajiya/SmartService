package com.smartservice.security.refreshToken;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenCleanupSchedulerTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenCleanupScheduler cleanupScheduler;

    @Test
    @DisplayName("Scheduled task invokes repository delete query with 30-day cutoff")
    void testCleanupRevokedTokens() {
        ReflectionTestUtils.setField(cleanupScheduler, "retentionDays", 30);
        when(refreshTokenRepository.deleteRevokedTokensOlderThan(any(Instant.class))).thenReturn(5);

        cleanupScheduler.cleanupRevokedTokens();

        ArgumentCaptor<Instant> cutoffCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(refreshTokenRepository, times(1)).deleteRevokedTokensOlderThan(cutoffCaptor.capture());

        Instant cutoff = cutoffCaptor.getValue();
        assertNotNull(cutoff);
        // Cutoff should be approximately 30 days ago
        long daysDiff = (Instant.now().getEpochSecond() - cutoff.getEpochSecond()) / (24 * 3600);
        assertEquals(30, daysDiff);
    }
}
