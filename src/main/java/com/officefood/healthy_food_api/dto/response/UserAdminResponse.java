package com.officefood.healthy_food_api.dto.response;

import lombok.*;
import java.time.ZonedDateTime;
import java.time.LocalDate;

@Data
public class UserAdminResponse {
    private java.util.UUID id;
    private String fullName;
    private String email;
    private String goalCode;
    private String role;
    private String status;
    private String imageUrl;
    private LocalDate dateOfBirth;
    private String address;
    private String phone;
    private ZonedDateTime createdAt;
}

