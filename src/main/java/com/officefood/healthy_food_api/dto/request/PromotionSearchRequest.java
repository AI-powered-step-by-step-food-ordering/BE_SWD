package com.officefood.healthy_food_api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionSearchRequest {

    // Exact match search
    private String promotionId;
    private String code; // Case-insensitive exact match

    // Partial match search (case-insensitive) - MAIN SEARCH FIELD
    private String name;

    // Status search (computed: active, expired, upcoming)
    private String status; // "active", "expired", "upcoming", "all"
}

