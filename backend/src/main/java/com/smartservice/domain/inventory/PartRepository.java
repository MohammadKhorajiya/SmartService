package com.smartservice.domain.inventory;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PartRepository extends JpaRepository<Part, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Part p WHERE p.id = :id")
    Optional<Part> findByIdForUpdate(@Param("id") Long id);

    Optional<Part> findBySku(String sku);

    @Query("SELECT p FROM Part p WHERE " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:active IS NULL OR p.active = :active) AND " +
           "(:query IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.compatibleDevice) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Part> searchParts(@Param("category") String category, @Param("active") Boolean active, @Param("query") String query, Pageable pageable);

    long countByQuantityInStockLessThanEqual(int reorderLevel);
}
