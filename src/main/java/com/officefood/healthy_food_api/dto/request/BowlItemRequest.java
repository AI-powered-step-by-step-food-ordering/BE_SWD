package com.officefood.healthy_food_api.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
public class BowlItemRequest {
    private Double quantity;
    private Double unitPrice;
    @NotNull private java.util.UUID bowlId;
    @NotNull private java.util.UUID ingredientId;
}
