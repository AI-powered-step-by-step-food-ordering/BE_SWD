package com.officefood.healthy_food_api.dto.request;

import com.officefood.healthy_food_api.model.enums.RestrictionType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IngredientRestrictionRequest {
    @NotNull
    private String primaryIngredientId;

    @NotNull
    private String restrictedIngredientId;

    @NotNull
    private RestrictionType type;

    private String reason;

    private Boolean isActive = true;
}
