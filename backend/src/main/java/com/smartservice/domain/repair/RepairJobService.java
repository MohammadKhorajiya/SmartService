package com.smartservice.domain.repair;

import com.smartservice.common.dto.PageResponse;
import com.smartservice.common.exception.BusinessRuleException;
import com.smartservice.common.exception.ResourceNotFoundException;
import com.smartservice.common.exception.UnauthorizedAccessException;
import com.smartservice.common.util.SecurityUtils;
import com.smartservice.domain.customer.Customer;
import com.smartservice.domain.customer.CustomerRepository;
import com.smartservice.domain.device.Device;
import com.smartservice.domain.device.DeviceRepository;
import com.smartservice.domain.repair.dto.*;
import com.smartservice.domain.servicerequest.ServiceRequest;
import com.smartservice.domain.servicerequest.ServiceRequestRepository;
import com.smartservice.domain.technician.Technician;
import com.smartservice.domain.technician.TechnicianRepository;
import com.smartservice.domain.user.Role;
import com.smartservice.domain.user.User;
import com.smartservice.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class RepairJobService {

    private final RepairJobRepository repairJobRepository;
    private final RepairJobStatusHistoryRepository historyRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final CustomerRepository customerRepository;
    private final DeviceRepository deviceRepository;
    private final TechnicianRepository technicianRepository;
    private final UserRepository userRepository;
    private final RepairJobStatusTransitionValidator transitionValidator;

    @Transactional
    public RepairJobDTO createJob(CreateRepairJobRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));

        Device device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", request.getDeviceId()));

        Technician technician = null;
        if (request.getTechnicianId() != null) {
            technician = technicianRepository.findById(request.getTechnicianId())
                    .orElseThrow(() -> new ResourceNotFoundException("Technician", "id", request.getTechnicianId()));
        }

        ServiceRequest serviceRequest = null;
        if (request.getServiceRequestId() != null) {
            serviceRequest = serviceRequestRepository.findById(request.getServiceRequestId())
                    .orElseThrow(() -> new ResourceNotFoundException("ServiceRequest", "id", request.getServiceRequestId()));
            serviceRequest.setStatus("CONVERTED_TO_JOB");
            serviceRequestRepository.save(serviceRequest);
        }

        String jobNumber = generateJobNumber();
        RepairJobStatus initialStatus = technician != null ? RepairJobStatus.ASSIGNED : RepairJobStatus.REQUESTED;

        RepairJob job = RepairJob.builder()
                .jobNumber(jobNumber)
                .serviceRequest(serviceRequest)
                .customer(customer)
                .device(device)
                .technician(technician)
                .status(initialStatus)
                .priority(request.getPriority() != null ? request.getPriority() : "MEDIUM")
                .build();

        job = repairJobRepository.save(job);
        recordHistory(job, null, initialStatus, "Job created");

        return mapToDTO(job);
    }

    @Transactional
    public RepairJobDTO assignTechnician(Long jobId, AssignTechnicianRequest request) {
        RepairJob job = repairJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("RepairJob", "id", jobId));

        Technician technician = technicianRepository.findById(request.getTechnicianId())
                .orElseThrow(() -> new ResourceNotFoundException("Technician", "id", request.getTechnicianId()));

        RepairJobStatus oldStatus = job.getStatus();
        RepairJobStatus newStatus = (oldStatus == RepairJobStatus.REQUESTED) ? RepairJobStatus.ASSIGNED : oldStatus;

        if (oldStatus != newStatus) {
            transitionValidator.validateTransition(oldStatus, newStatus);
        }

        job.setTechnician(technician);
        job.setStatus(newStatus);
        job = repairJobRepository.save(job);

        recordHistory(job, oldStatus, newStatus, "Technician assigned: " + technician.getName());

        return mapToDTO(job);
    }

    @Transactional
    public RepairJobDTO updateStatus(Long jobId, UpdateJobStatusRequest request) {
        RepairJob job = repairJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("RepairJob", "id", jobId));

        // Security isolation check: Technicians can only update jobs assigned to them
        if (SecurityUtils.isTechnician()) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            Technician tech = technicianRepository.findByUserId(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Technician record not found"));
            if (job.getTechnician() == null || !job.getTechnician().getId().equals(tech.getId())) {
                throw new UnauthorizedAccessException("You can only update status for jobs assigned to you");
            }
        }

        RepairJobStatus oldStatus = job.getStatus();
        RepairJobStatus newStatus = request.getNewStatus();

        transitionValidator.validateTransition(oldStatus, newStatus);

        job.setStatus(newStatus);
        if (newStatus == RepairJobStatus.IN_REPAIR && job.getStartedAt() == null) {
            job.setStartedAt(LocalDateTime.now());
        } else if (newStatus == RepairJobStatus.COMPLETED) {
            job.setCompletedAt(LocalDateTime.now());
        }

        job = repairJobRepository.save(job);
        recordHistory(job, oldStatus, newStatus, request.getRemarks());

        return mapToDTO(job);
    }

    @Transactional(readOnly = true)
    public PageResponse<RepairJobDTO> filterJobs(RepairJobStatus status, Long technicianId, Long customerId, String query, Pageable pageable) {
        final Long targetCustomerId;
        final Long targetTechnicianId;

        if (SecurityUtils.isCustomer()) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            Customer customer = customerRepository.findByUserId(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));
            targetCustomerId = customer.getId();
            targetTechnicianId = technicianId;
        } else if (SecurityUtils.isTechnician()) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            Technician tech = technicianRepository.findByUserId(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Technician profile not found"));
            targetTechnicianId = tech.getId();
            targetCustomerId = customerId;
        } else {
            targetCustomerId = customerId;
            targetTechnicianId = technicianId;
        }

        Page<RepairJob> page = repairJobRepository.filterJobs(status, targetTechnicianId, targetCustomerId, query, pageable);
        return PageResponse.from(page.map(this::mapToDTO));
    }

    @Transactional(readOnly = true)
    public RepairJobDTO getJobById(Long id) {
        RepairJob job = repairJobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RepairJob", "id", id));

        if (SecurityUtils.isCustomer()) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (job.getCustomer().getUser() == null || !job.getCustomer().getUser().getId().equals(currentUserId)) {
                throw new UnauthorizedAccessException("Access denied to repair job details");
            }
        }

        return mapToDTO(job);
    }

    @Transactional(readOnly = true)
    public List<RepairJobStatusHistoryDTO> getJobStatusHistory(Long jobId) {
        getJobById(jobId); // Ensures isolation check
        return historyRepository.findByRepairJobIdOrderByCreatedAtDesc(jobId).stream()
                .map(this::mapHistoryToDTO)
                .toList();
    }

    private void recordHistory(RepairJob job, RepairJobStatus oldStatus, RepairJobStatus newStatus, String remarks) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User changedByUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;

        RepairJobStatusHistory history = RepairJobStatusHistory.builder()
                .repairJob(job)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedByUser(changedByUser)
                .remarks(remarks)
                .build();

        historyRepository.save(history);
    }

    private String generateJobNumber() {
        return "RJ-" + (100000 + new Random().nextInt(900000));
    }

    public RepairJobDTO mapToDTO(RepairJob job) {
        return RepairJobDTO.builder()
                .id(job.getId())
                .jobNumber(job.getJobNumber())
                .serviceRequestId(job.getServiceRequest() != null ? job.getServiceRequest().getId() : null)
                .customerId(job.getCustomer().getId())
                .customerName(job.getCustomer().getName())
                .customerPhone(job.getCustomer().getPhone())
                .customerEmail(job.getCustomer().getEmail())
                .deviceId(job.getDevice().getId())
                .deviceBrand(job.getDevice().getBrand())
                .deviceModel(job.getDevice().getModel())
                .deviceSerialNumber(job.getDevice().getSerialNumber())
                .deviceImei(job.getDevice().getImei())
                .technicianId(job.getTechnician() != null ? job.getTechnician().getId() : null)
                .technicianName(job.getTechnician() != null ? job.getTechnician().getName() : null)
                .status(job.getStatus())
                .priority(job.getPriority())
                .laborCost(job.getLaborCost())
                .totalEstimatedCost(job.getTotalEstimatedCost())
                .totalActualCost(job.getTotalActualCost())
                .createdAt(job.getCreatedAt())
                .startedAt(job.getStartedAt())
                .completedAt(job.getCompletedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }

    private RepairJobStatusHistoryDTO mapHistoryToDTO(RepairJobStatusHistory history) {
        return RepairJobStatusHistoryDTO.builder()
                .id(history.getId())
                .repairJobId(history.getRepairJob().getId())
                .oldStatus(history.getOldStatus())
                .newStatus(history.getNewStatus())
                .changedByUserId(history.getChangedByUser() != null ? history.getChangedByUser().getId() : null)
                .changedByUserName(history.getChangedByUser() != null ? history.getChangedByUser().getFullName() : "System")
                .remarks(history.getRemarks())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
