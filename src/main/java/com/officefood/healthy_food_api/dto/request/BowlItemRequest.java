package com.officefood.healthy_food_api.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
public class BowlItemRequest {
    @NotNull private Double quantity; // Số lượng khách muốn (VD: 150g)
    // unitPrice sẽ tự động lấy từ Ingredient (snapshot)
    @NotNull private java.util.UUID bowlId;
    @NotNull private java.util.UUID ingredientId;
}
