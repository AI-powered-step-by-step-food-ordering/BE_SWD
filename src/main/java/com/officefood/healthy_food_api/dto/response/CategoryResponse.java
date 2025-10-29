package com.officefood.healthy_food_api.dto.response;

import lombok.*;
import java.time.ZonedDateTime;

@Data
public class CategoryResponse {
    private java.util.UUID id;
    private String name;
    private String kind;
    private Boolean isActive;
    private String imageUrl;
    private ZonedDateTime createdAt;
}
