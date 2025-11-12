package com.officefood.healthy_food_api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorySearchRequest {

    // Exact match search
    private String categoryId;

    // Partial match search (case-insensitive) - MAIN SEARCH FIELD
    private String name;
}

