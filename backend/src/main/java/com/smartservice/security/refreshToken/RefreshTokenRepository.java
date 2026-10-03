package com.smartservice.security.refreshToken;

import com.smartservice.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    int deleteByUser(User user);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.revoked = true AND (r.createdAt < :cutoff OR (r.createdAt IS NULL AND r.expiryDate < :cutoff))")
    int deleteRevokedTokensOlderThan(@Param("cutoff") Instant cutoff);
}
