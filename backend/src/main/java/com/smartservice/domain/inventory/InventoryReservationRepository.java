package com.smartservice.domain.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {
    List<InventoryReservation> findByRepairJobIdAndStatus(Long repairJobId, String status);
    Optional<InventoryReservation> findByPartIdAndRepairJobIdAndStatus(Long partId, Long repairJobId, String status);
}
