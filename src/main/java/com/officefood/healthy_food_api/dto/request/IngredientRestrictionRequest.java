package com.officefood.healthy_food_api.dto.request;

import com.officefood.healthy_food_api.model.enums.RestrictionType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class IngredientRestrictionRequest {
    @NotNull
    private UUID primaryIngredientId;

    @NotNull
    private UUID restrictedIngredientId;

    @NotNull
    private RestrictionType type;

    private String reason;

    private Boolean isActive = true;
}
