package com.officefood.healthy_food_api.dto.request;

import com.officefood.healthy_food_api.model.enums.PromotionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionSearchRequest {

    // Exact match search
    private String promotionId;
    private String code; // Case-insensitive exact match
    private PromotionType type;

    // Partial match search (case-insensitive) - MAIN SEARCH FIELD
    private String name;

    // Multi-select type - will be set from controller
    private List<PromotionType> types;

    // Status search (computed: active, expired, upcoming)
    private String status; // "active", "expired", "upcoming", "all"

    /**
     * Helper method to set types from String array
     * Supports: ?types=PERCENT_OFF&types=AMOUNT_OFF or ?types=PERCENT_OFF,AMOUNT_OFF
     */
    public void setTypesFromArray(String[] typesArray) {
        if (typesArray != null && typesArray.length > 0) {
            this.types = Arrays.stream(typesArray)
                .flatMap(s -> Arrays.stream(s.split(",")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    try {
                        return PromotionType.valueOf(s.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(type -> type != null)
                .collect(Collectors.toList());
        } else {
            this.types = new ArrayList<>();
        }
    }
}

