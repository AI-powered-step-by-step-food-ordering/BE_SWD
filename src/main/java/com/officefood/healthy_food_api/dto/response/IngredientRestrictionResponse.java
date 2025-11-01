package com.officefood.healthy_food_api.dto.response;

import com.officefood.healthy_food_api.model.enums.RestrictionType;
import lombok.Data;

@Data
public class IngredientRestrictionResponse {
    private String id;
    private String primaryIngredientId;
    private String primaryIngredientName;
    private String restrictedIngredientId;
    private String restrictedIngredientName;
    private RestrictionType type;
    private String reason;
    private Boolean active;
}
