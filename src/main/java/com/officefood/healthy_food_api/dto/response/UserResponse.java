package com.officefood.healthy_food_api.dto.response;

import lombok.*;

@Data
public class UserResponse {
    private java.util.UUID id;
    private String fullName;
    private String email;
    private String goalCode;
    private String role;
    private String status;
}
