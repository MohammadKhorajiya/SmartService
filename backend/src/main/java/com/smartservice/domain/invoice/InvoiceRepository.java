package com.smartservice.domain.invoice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByRepairJobId(Long repairJobId);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    @Query("SELECT i FROM Invoice i WHERE " +
           "(:status IS NULL OR i.paymentStatus = :status) AND " +
           "(:customerId IS NULL OR i.customer.id = :customerId)")
    Page<Invoice> filterInvoices(@Param("status") String status, @Param("customerId") Long customerId, Pageable pageable);
}
