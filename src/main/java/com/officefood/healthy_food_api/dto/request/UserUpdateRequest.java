package com.officefood.healthy_food_api.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
public class UserUpdateRequest {
    @NotBlank private String fullName;
    @NotBlank private String email;
    private String goalCode;
    private String imageUrl; // Avatar/profile picture URL
    // Note: status is not included - use /soft-delete or /restore endpoints
}

