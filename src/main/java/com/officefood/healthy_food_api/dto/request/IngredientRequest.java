package com.officefood.healthy_food_api.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
public class IngredientRequest {
    @NotBlank private String name;
    private String unit; // VD: "g", "ml", "piece"
    private Double standardQuantity; // VD: 100, 50
    private Double unitPrice; // GiÃ¡ cho standardQuantity
    @NotNull private String categoryId;
    private String imageUrl; // Ingredient image URL
}
