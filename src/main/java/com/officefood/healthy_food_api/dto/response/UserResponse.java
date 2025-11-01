package com.officefood.healthy_food_api.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.ZonedDateTime;
import java.time.LocalDate;

@Data
public class UserResponse {
    private String id;
    private String fullName;
    private String email;
    private String goalCode;
    private String role;
    private String status;
    private String imageUrl;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    private String address;
    private String phone;
    // createdAt removed - only for admin response
}
