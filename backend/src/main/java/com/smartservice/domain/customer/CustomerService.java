package com.smartservice.domain.customer;

import com.smartservice.common.dto.PageResponse;
import com.smartservice.common.exception.ResourceNotFoundException;
import com.smartservice.common.exception.UnauthorizedAccessException;
import com.smartservice.common.util.SecurityUtils;
import com.smartservice.domain.customer.dto.CreateCustomerRequest;
import com.smartservice.domain.customer.dto.CustomerDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerDTO createCustomer(CreateCustomerRequest request) {
        Customer customer = Customer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();

        customer = customerRepository.save(customer);
        return mapToDTO(customer);
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerDTO> searchCustomers(String query, Pageable pageable) {
        Page<Customer> page = customerRepository.searchCustomers(query, pageable);
        return PageResponse.from(page.map(this::mapToDTO));
    }

    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        if (SecurityUtils.isCustomer()) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (customer.getUser() == null || !customer.getUser().getId().equals(currentUserId)) {
                throw new UnauthorizedAccessException("Access denied to customer record");
            }
        }

        return mapToDTO(customer);
    }

    @Transactional
    public CustomerDTO updateCustomer(Long id, CreateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        if (SecurityUtils.isCustomer()) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (customer.getUser() == null || !customer.getUser().getId().equals(currentUserId)) {
                throw new UnauthorizedAccessException("Access denied to update customer record");
            }
        }

        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());

        customer = customerRepository.save(customer);
        return mapToDTO(customer);
    }

    public CustomerDTO mapToDTO(Customer customer) {
        return CustomerDTO.builder()
                .id(customer.getId())
                .userId(customer.getUser() != null ? customer.getUser().getId() : null)
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
