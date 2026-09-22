package com.smartservice.common.util;

import com.smartservice.domain.user.Role;
import com.smartservice.security.userDetails.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityUtils {

    public static Optional<CustomUserDetails> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return Optional.of((CustomUserDetails) authentication.getPrincipal());
        }
        return Optional.empty();
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().map(CustomUserDetails::getId).orElse(null);
    }

    public static String getCurrentUserEmail() {
        return getCurrentUser().map(CustomUserDetails::getEmail).orElse(null);
    }

    public static Role getCurrentUserRole() {
        return getCurrentUser().map(CustomUserDetails::getRole).orElse(null);
    }

    public static boolean isAdminOrManager() {
        Role role = getCurrentUserRole();
        return role == Role.ADMIN || role == Role.MANAGER;
    }

    public static boolean isTechnician() {
        return getCurrentUserRole() == Role.TECHNICIAN;
    }

    public static boolean isCustomer() {
        return getCurrentUserRole() == Role.CUSTOMER;
    }
}
