package com.officefood.healthy_food_api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BowlTemplateSearchRequest {

    // Exact match search
    private String templateId;

    // Partial match search (case-insensitive) - MAIN SEARCH FIELD
    private String name;
}

