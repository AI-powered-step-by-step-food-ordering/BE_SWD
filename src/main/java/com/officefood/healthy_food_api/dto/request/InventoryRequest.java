package com.officefood.healthy_food_api.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
public class InventoryRequest {
    @NotBlank private String action;
    private Double quantityChange;
    private String note;
    @NotNull private java.util.UUID storeId;
    @NotNull private java.util.UUID ingredientId;
}
