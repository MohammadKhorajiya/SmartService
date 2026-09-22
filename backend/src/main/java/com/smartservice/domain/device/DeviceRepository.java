package com.smartservice.domain.device;

import com.smartservice.domain.customer.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    List<Device> findByCustomer(Customer customer);
    Page<Device> findByCustomerId(Long customerId, Pageable pageable);
}
