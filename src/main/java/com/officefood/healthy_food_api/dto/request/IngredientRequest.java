package com.officefood.healthy_food_api.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
public class IngredientRequest {
    @NotBlank private String name;
    @NotBlank private String kind;
    private String unit;
    private Double unitPrice;
    @NotNull private java.util.UUID categoryId;
}
