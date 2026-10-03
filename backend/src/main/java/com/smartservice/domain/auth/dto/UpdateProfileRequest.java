package com.smartservice.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    private String phone;

    private String address;

    private String currentPassword;

    @Size(min = 6, message = "New password must be at least 6 characters")
    private String newPassword;
}
