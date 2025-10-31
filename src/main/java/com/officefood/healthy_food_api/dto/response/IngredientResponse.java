package com.officefood.healthy_food_api.dto.response;

import lombok.*;
import java.time.ZonedDateTime;

@Data
public class IngredientResponse {
    private java.util.UUID id;
    private String name;
    private String unit; // VD: "g", "ml", "piece"
    private Double standardQuantity; // VD: 100, 50
    private Double unitPrice; // Giá cho standardQuantity
    private java.util.UUID categoryId;
    private String imageUrl;
    private boolean active;
    private ZonedDateTime createdAt;
}
