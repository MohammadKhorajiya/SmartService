package com.smartservice.domain.auth.dto;

import com.smartservice.domain.user.Role;
import com.smartservice.domain.user.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {

    private Long userId;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private Role role;
    private UserStatus status;
    private LocalDateTime createdAt;
}
