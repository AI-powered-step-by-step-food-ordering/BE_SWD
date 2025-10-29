package com.officefood.healthy_food_api.dto.response;

import lombok.*;
import java.time.ZonedDateTime;

@Data
public class IngredientResponse {
    private java.util.UUID id;
    private String name;
    private String unit;
    private Double unitPrice;
    private java.util.UUID categoryId;
    private String imageUrl;
    private ZonedDateTime createdAt;
}
