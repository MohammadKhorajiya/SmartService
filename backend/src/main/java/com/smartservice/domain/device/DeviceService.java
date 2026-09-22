package com.smartservice.domain.device;

import com.smartservice.common.dto.PageResponse;
import com.smartservice.common.exception.ResourceNotFoundException;
import com.smartservice.common.exception.UnauthorizedAccessException;
import com.smartservice.common.util.SecurityUtils;
import com.smartservice.domain.customer.Customer;
import com.smartservice.domain.customer.CustomerRepository;
import com.smartservice.domain.device.dto.CreateDeviceRequest;
import com.smartservice.domain.device.dto.DeviceDTO;
import com.smartservice.domain.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public DeviceDTO createDevice(CreateDeviceRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));

        // Security check: Customer can only add devices for themselves
        if (SecurityUtils.isCustomer()) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (customer.getUser() == null || !customer.getUser().getId().equals(currentUserId)) {
                throw new UnauthorizedAccessException("You can only add devices for your own account");
            }
        }

        Device device = Device.builder()
                .customer(customer)
                .brand(request.getBrand())
                .model(request.getModel())
                .serialNumber(request.getSerialNumber())
                .imei(request.getImei())
                .deviceType(request.getDeviceType())
                .build();

        device = deviceRepository.save(device);
        return mapToDTO(device);
    }

    @Transactional(readOnly = true)
    public PageResponse<DeviceDTO> getCustomerDevices(Long customerId, Pageable pageable) {
        final Long targetCustomerId;
        if (SecurityUtils.isCustomer()) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            Customer customer = customerRepository.findByUserId(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer record not found for logged in user"));
            targetCustomerId = customer.getId();
        } else {
            targetCustomerId = customerId;
        }

        Page<Device> page = deviceRepository.findByCustomerId(targetCustomerId, pageable);
        return PageResponse.from(page.map(this::mapToDTO));
    }

    @Transactional(readOnly = true)
    public List<DeviceDTO> getCustomerDevicesList(Long customerId) {
        final Long targetCustomerId;
        if (SecurityUtils.isCustomer()) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            Customer customer = customerRepository.findByUserId(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer record not found for logged in user"));
            targetCustomerId = customer.getId();
        } else {
            targetCustomerId = customerId;
        }

        Customer customer = customerRepository.findById(targetCustomerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", targetCustomerId));

        return deviceRepository.findByCustomer(customer).stream().map(this::mapToDTO).toList();
    }

    @Transactional(readOnly = true)
    public DeviceDTO getDeviceById(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", id));

        if (SecurityUtils.isCustomer()) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (device.getCustomer().getUser() == null || !device.getCustomer().getUser().getId().equals(currentUserId)) {
                throw new UnauthorizedAccessException("Access denied to device details");
            }
        }

        return mapToDTO(device);
    }

    public DeviceDTO mapToDTO(Device device) {
        return DeviceDTO.builder()
                .id(device.getId())
                .customerId(device.getCustomer().getId())
                .customerName(device.getCustomer().getName())
                .brand(device.getBrand())
                .model(device.getModel())
                .serialNumber(device.getSerialNumber())
                .imei(device.getImei())
                .deviceType(device.getDeviceType())
                .createdAt(device.getCreatedAt())
                .build();
    }
}
