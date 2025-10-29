package com.officefood.healthy_food_api.dto.response;

import lombok.*;

@Data
public class BowlTemplateResponse {
    private java.util.UUID id;
    private String name;
    private String description;
    private Boolean isActive;
    private String imageUrl; // Bowl template/menu image URL
}
