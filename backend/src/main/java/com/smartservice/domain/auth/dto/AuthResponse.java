package com.smartservice.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.smartservice.domain.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long userId;
    private String email;
    private String fullName;
    private Role role;
    private Long customerId;    // Populated if user has linked Customer record
    private Long technicianId;  // Populated if user has linked Technician record
}
