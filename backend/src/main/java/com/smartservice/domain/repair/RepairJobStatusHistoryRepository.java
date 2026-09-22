package com.smartservice.domain.repair;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepairJobStatusHistoryRepository extends JpaRepository<RepairJobStatusHistory, Long> {
    List<RepairJobStatusHistory> findByRepairJobIdOrderByCreatedAtDesc(Long repairJobId);
}
