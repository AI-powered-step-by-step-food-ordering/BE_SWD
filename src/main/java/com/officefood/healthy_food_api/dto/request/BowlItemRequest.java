package com.officefood.healthy_food_api.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
public class BowlItemRequest {
    @NotNull private Double quantity;
    @NotNull private String bowlId;
    @NotNull private String ingredientId;
}
