package com.officefood.healthy_food_api.dto.response;

import com.officefood.healthy_food_api.model.enums.RestrictionType;
import lombok.Data;

import java.util.UUID;

@Data
public class IngredientRestrictionResponse {
    private UUID id;
    private UUID primaryIngredientId;
    private String primaryIngredientName;
    private UUID restrictedIngredientId;
    private String restrictedIngredientName;
    private RestrictionType type;
    private String reason;
    private Boolean active;
}
