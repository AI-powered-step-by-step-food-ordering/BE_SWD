package com.officefood.healthy_food_api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BowlSearchRequest {

    // Exact match search
    private String bowlId;
    private String orderId;
    private String templateId;

    // Partial match search (case-insensitive) - MAIN SEARCH FIELD
    private String name;
}

