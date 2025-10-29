package com.officefood.healthy_food_api.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
public class UserRequest {
    @NotBlank private String fullName;
    @NotBlank private String email;
    @NotBlank private String password;
    private String goalCode;
    private String role;
    private String status;
    private String imageUrl;
    private LocalDate dateOfBirth;
    private String address;
    private String phone;
}
