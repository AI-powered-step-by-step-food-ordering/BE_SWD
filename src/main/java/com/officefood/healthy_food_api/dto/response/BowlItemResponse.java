package com.officefood.healthy_food_api.dto.response;

import lombok.*;

@Data
public class BowlItemResponse {
    private String id;
    private String bowlId;
    private String ingredientId;
    private IngredientResponse ingredient;
    private Double quantity;
    private Double unitPrice;
}
