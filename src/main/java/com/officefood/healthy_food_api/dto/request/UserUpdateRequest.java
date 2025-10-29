package com.officefood.healthy_food_api.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
public class UserUpdateRequest {
    @NotBlank private String fullName;
    @NotBlank private String email;
    private String goalCode;
    private String imageUrl;
    private LocalDate dateOfBirth;
    private String address;
    private String phone;
    // Note: status is not included - use /soft-delete or /restore endpoints
}

