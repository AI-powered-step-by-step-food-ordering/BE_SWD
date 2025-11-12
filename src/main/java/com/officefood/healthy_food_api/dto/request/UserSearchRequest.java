package com.officefood.healthy_food_api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchRequest {

    // Exact match search
    private String userId;

    // Partial match search (case-insensitive) - MAIN SEARCH FIELDS
    private String fullName;
    private String email;
    private String phone;
}

