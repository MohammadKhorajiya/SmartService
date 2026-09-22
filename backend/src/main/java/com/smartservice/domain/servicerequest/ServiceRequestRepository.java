package com.smartservice.domain.servicerequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    Page<ServiceRequest> findByCustomerId(Long customerId, Pageable pageable);

    @Query("SELECT sr FROM ServiceRequest sr WHERE " +
           "(:status IS NULL OR sr.status = :status) AND " +
           "(:customerId IS NULL OR sr.customer.id = :customerId)")
    Page<ServiceRequest> filterRequests(
            @Param("status") String status,
            @Param("customerId") Long customerId,
            Pageable pageable);
}
