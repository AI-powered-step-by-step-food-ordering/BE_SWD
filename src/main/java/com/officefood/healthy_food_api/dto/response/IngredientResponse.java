package com.officefood.healthy_food_api.dto.response;

import lombok.*;
import java.time.ZonedDateTime;

@Data
public class IngredientResponse {
    private String id;
    private String name;
    private String unit; // VD: "g", "ml", "piece"
    private Double standardQuantity; // VD: 100, 50
    private Double unitPrice; // GiÃƒÂ¡ cho standardQuantity
    private String categoryId;
    private String imageUrl;
    private boolean active;
    private ZonedDateTime createdAt;
}
