package com.smartservice.domain.estimate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstimateRepository extends JpaRepository<Estimate, Long> {
    Optional<Estimate> findByRepairJobId(Long repairJobId);
    List<Estimate> findByCustomerId(Long customerId);
}
