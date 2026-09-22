package com.smartservice.domain.servicerequest;

import com.smartservice.common.dto.PageResponse;
import com.smartservice.common.exception.ResourceNotFoundException;
import com.smartservice.common.exception.UnauthorizedAccessException;
import com.smartservice.common.util.SecurityUtils;
import com.smartservice.domain.customer.Customer;
import com.smartservice.domain.customer.CustomerRepository;
import com.smartservice.domain.device.Device;
import com.smartservice.domain.device.DeviceRepository;
import com.smartservice.domain.servicerequest.dto.CreateServiceRequest;
import com.smartservice.domain.servicerequest.dto.ServiceRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final CustomerRepository customerRepository;
    private final DeviceRepository deviceRepository;

    @Transactional
    public ServiceRequestDTO createRequest(CreateServiceRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Customer customer = customerRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for user ID: " + currentUserId));

        Device device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", request.getDeviceId()));

        if (!device.getCustomer().getId().equals(customer.getId())) {
            throw new UnauthorizedAccessException("Selected device does not belong to your account");
        }

        ServiceRequest serviceRequest = ServiceRequest.builder()
                .customer(customer)
                .device(device)
                .problemTitle(request.getProblemTitle())
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : "MEDIUM")
                .status("REQUESTED")
                .build();

        serviceRequest = serviceRequestRepository.save(serviceRequest);
        return mapToDTO(serviceRequest);
    }

    @Transactional(readOnly = true)
    public PageResponse<ServiceRequestDTO> getServiceRequests(String status, Long customerId, Pageable pageable) {
        final Long targetCustomerId;
        if (SecurityUtils.isCustomer()) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            Customer customer = customerRepository.findByUserId(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));
            targetCustomerId = customer.getId();
        } else {
            targetCustomerId = customerId;
        }

        Page<ServiceRequest> page = serviceRequestRepository.filterRequests(status, targetCustomerId, pageable);
        return PageResponse.from(page.map(this::mapToDTO));
    }

    @Transactional(readOnly = true)
    public ServiceRequestDTO getServiceRequestById(Long id) {
        ServiceRequest sr = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceRequest", "id", id));

        if (SecurityUtils.isCustomer()) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (sr.getCustomer().getUser() == null || !sr.getCustomer().getUser().getId().equals(currentUserId)) {
                throw new UnauthorizedAccessException("Access denied to service request details");
            }
        }

        return mapToDTO(sr);
    }

    public ServiceRequestDTO mapToDTO(ServiceRequest sr) {
        return ServiceRequestDTO.builder()
                .id(sr.getId())
                .customerId(sr.getCustomer().getId())
                .customerName(sr.getCustomer().getName())
                .customerPhone(sr.getCustomer().getPhone())
                .deviceId(sr.getDevice().getId())
                .deviceBrand(sr.getDevice().getBrand())
                .deviceModel(sr.getDevice().getModel())
                .problemTitle(sr.getProblemTitle())
                .description(sr.getDescription())
                .priority(sr.getPriority())
                .status(sr.getStatus())
                .createdAt(sr.getCreatedAt())
                .build();
    }
}
