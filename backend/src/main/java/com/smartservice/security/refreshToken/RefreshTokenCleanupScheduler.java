package com.smartservice.security.refreshToken;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.revoked-token-retention-days:30}")
    private int retentionDays;

    @Scheduled(cron = "${app.jwt.revoked-token-cleanup-cron:0 0 3 * * ?}")
    @Transactional
    public void cleanupRevokedTokens() {
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        int deletedCount = refreshTokenRepository.deleteRevokedTokensOlderThan(cutoff);
        log.info("Revoked refresh token cleanup completed: deleted {} tokens older than {} days (cutoff: {})",
                deletedCount, retentionDays, cutoff);
    }
}
