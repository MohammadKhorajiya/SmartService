package com.smartservice.domain.analytics;

import com.smartservice.domain.analytics.dto.DashboardSummaryDTO;
import com.smartservice.domain.customer.CustomerRepository;
import com.smartservice.domain.inventory.PartRepository;
import com.smartservice.domain.invoice.Invoice;
import com.smartservice.domain.invoice.InvoiceRepository;
import com.smartservice.domain.repair.RepairJobRepository;
import com.smartservice.domain.repair.RepairJobStatus;
import lombok.RequiredArgsConstructor;
import com.smartservice.domain.customer.Customer;
import com.smartservice.domain.user.Role;
import com.smartservice.security.userDetails.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CustomerRepository customerRepository;
    private final RepairJobRepository repairJobRepository;
    private final PartRepository partRepository;
    private final InvoiceRepository invoiceRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryDTO getDashboardSummary() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            if (userDetails.getRole() == Role.CUSTOMER) {
                Long customerId = customerRepository.findByUserId(userDetails.getId())
                        .map(Customer::getId)
                        .orElse(null);

                long completedRepairs = 0;
                long activeRepairs = 0;
                long pendingEstimates = 0;

                if (customerId != null) {
                    completedRepairs = repairJobRepository.countByCustomerIdAndStatus(customerId, RepairJobStatus.COMPLETED);
                    activeRepairs = repairJobRepository.countByCustomerId(customerId) - completedRepairs - repairJobRepository.countByCustomerIdAndStatus(customerId, RepairJobStatus.CANCELLED);
                    pendingEstimates = repairJobRepository.countByCustomerIdAndStatus(customerId, RepairJobStatus.WAITING_FOR_APPROVAL);
                }

                return DashboardSummaryDTO.builder()
                        .totalCustomers(1)
                        .activeRepairs(activeRepairs)
                        .pendingEstimates(pendingEstimates)
                        .completedRepairs(completedRepairs)
                        .totalRevenue(BigDecimal.ZERO)
                        .lowStockCount(0)
                        .repairsByStatus(new HashMap<>())
                        .build();
            }
        }

        // Full Admin/Manager/Staff metrics
        long totalCustomers = customerRepository.count();

        long completedRepairs = repairJobRepository.countByStatus(RepairJobStatus.COMPLETED);
        long activeRepairs = repairJobRepository.count() - completedRepairs - repairJobRepository.countByStatus(RepairJobStatus.CANCELLED);
        long pendingEstimates = repairJobRepository.countByStatus(RepairJobStatus.WAITING_FOR_APPROVAL);
        long lowStockCount = partRepository.countByQuantityInStockLessThanEqual(5);

        BigDecimal totalRevenue = invoiceRepository.findAll().stream()
                .filter(inv -> "PAID".equalsIgnoreCase(inv.getPaymentStatus()))
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Long> repairsByStatus = new HashMap<>();
        for (RepairJobStatus status : RepairJobStatus.values()) {
            long count = repairJobRepository.countByStatus(status);
            if (count > 0) {
                repairsByStatus.put(status.name(), count);
            }
        }

        return DashboardSummaryDTO.builder()
                .totalCustomers(totalCustomers)
                .activeRepairs(activeRepairs)
                .pendingEstimates(pendingEstimates)
                .completedRepairs(completedRepairs)
                .totalRevenue(totalRevenue)
                .lowStockCount(lowStockCount)
                .repairsByStatus(repairsByStatus)
                .build();
    }

}
