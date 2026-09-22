package com.smartservice.domain.repair;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepairJobRepository extends JpaRepository<RepairJob, Long> {

    @Query("SELECT r FROM RepairJob r WHERE " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:technicianId IS NULL OR r.technician.id = :technicianId) AND " +
           "(:customerId IS NULL OR r.customer.id = :customerId) AND " +
           "(:query IS NULL OR LOWER(r.jobNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.customer.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.device.model) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<RepairJob> filterJobs(
            @Param("status") RepairJobStatus status,
            @Param("technicianId") Long technicianId,
            @Param("customerId") Long customerId,
            @Param("query") String query,
            Pageable pageable);

    List<RepairJob> findByTechnicianIdAndStatusIn(Long technicianId, List<RepairJobStatus> statuses);

    long countByStatus(RepairJobStatus status);

    long countByCustomerId(Long customerId);

    long countByCustomerIdAndStatus(Long customerId, RepairJobStatus status);
}

