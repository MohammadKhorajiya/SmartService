package com.smartservice.domain.diagnosis;

import com.smartservice.common.exception.ResourceNotFoundException;
import com.smartservice.common.exception.UnauthorizedAccessException;
import com.smartservice.common.util.SecurityUtils;
import com.smartservice.domain.diagnosis.dto.CreateDiagnosisRequest;
import com.smartservice.domain.diagnosis.dto.DiagnosisDTO;
import com.smartservice.domain.repair.RepairJob;
import com.smartservice.domain.repair.RepairJobRepository;
import com.smartservice.domain.repair.RepairJobStatus;
import com.smartservice.domain.repair.RepairJobStatusTransitionValidator;
import com.smartservice.domain.technician.Technician;
import com.smartservice.domain.technician.TechnicianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DiagnosisService {

    private final DiagnosisRepository diagnosisRepository;
    private final RepairJobRepository repairJobRepository;
    private final TechnicianRepository technicianRepository;
    private final RepairJobStatusTransitionValidator transitionValidator;

    @Transactional
    public DiagnosisDTO addDiagnosis(CreateDiagnosisRequest request) {
        RepairJob job = repairJobRepository.findById(request.getRepairJobId())
                .orElseThrow(() -> new ResourceNotFoundException("RepairJob", "id", request.getRepairJobId()));

        Long currentUserId = SecurityUtils.getCurrentUserId();
        final Technician technician;
        if (SecurityUtils.isTechnician()) {
            Technician tech = technicianRepository.findByUserId(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Technician record not found"));
            if (job.getTechnician() == null || !job.getTechnician().getId().equals(tech.getId())) {
                throw new UnauthorizedAccessException("You can only diagnose repair jobs assigned to you");
            }
            technician = tech;
        } else if (job.getTechnician() != null) {
            technician = job.getTechnician();
        } else {
            technician = technicianRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("No active technician found"));
            job.setTechnician(technician);
        }

        // Validate state transition DIAGNOSING -> ESTIMATE_CREATED
        if (job.getStatus() == RepairJobStatus.ASSIGNED) {
            transitionValidator.validateTransition(RepairJobStatus.ASSIGNED, RepairJobStatus.DIAGNOSING);
            job.setStatus(RepairJobStatus.DIAGNOSING);
        }

        if (request.getEstimatedLaborCost() != null) {
            job.setLaborCost(request.getEstimatedLaborCost());
        }

        job = repairJobRepository.save(job);

        final RepairJob targetJob = job;
        final Technician targetTech = technician;

        Diagnosis diagnosis = diagnosisRepository.findByRepairJobId(targetJob.getId())
                .orElseGet(() -> Diagnosis.builder().repairJob(targetJob).technician(targetTech).build());

        diagnosis.setSymptoms(request.getSymptoms());
        diagnosis.setFindings(request.getFindings());
        diagnosis.setRecommendedRepair(request.getRecommendedRepair());
        diagnosis.setEstimatedLaborCost(request.getEstimatedLaborCost());
        diagnosis.setNotes(request.getNotes());

        diagnosis = diagnosisRepository.save(diagnosis);
        return mapToDTO(diagnosis);
    }

    @Transactional(readOnly = true)
    public DiagnosisDTO getDiagnosisByJobId(Long jobId) {
        RepairJob job = repairJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("RepairJob", "id", jobId));

        if (SecurityUtils.isCustomer()) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (job.getCustomer().getUser() == null || !job.getCustomer().getUser().getId().equals(currentUserId)) {
                throw new UnauthorizedAccessException("Access denied to diagnosis details");
            }
        }

        Diagnosis diagnosis = diagnosisRepository.findByRepairJobId(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Diagnosis not found for job ID: " + jobId));

        return mapToDTO(diagnosis);
    }

    public DiagnosisDTO mapToDTO(Diagnosis diagnosis) {
        return DiagnosisDTO.builder()
                .id(diagnosis.getId())
                .repairJobId(diagnosis.getRepairJob().getId())
                .jobNumber(diagnosis.getRepairJob().getJobNumber())
                .technicianId(diagnosis.getTechnician().getId())
                .technicianName(diagnosis.getTechnician().getName())
                .symptoms(diagnosis.getSymptoms())
                .findings(diagnosis.getFindings())
                .recommendedRepair(diagnosis.getRecommendedRepair())
                .estimatedLaborCost(diagnosis.getEstimatedLaborCost())
                .notes(diagnosis.getNotes())
                .createdAt(diagnosis.getCreatedAt())
                .build();
    }
}
