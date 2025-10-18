package com.officefood.healthy_food_api.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
public class UserRequest {
    @NotBlank private String fullName;
    @NotBlank private String email;
    @NotBlank private String password;
    private String goalCode;
    private String role;
    private String status;
}
