package com.officefood.healthy_food_api.dto.response;

import lombok.*;

@Data
public class CategoryResponse {
    private java.util.UUID id;
    private String name;
    private String kind;
    private Integer displayOrder;
    private Boolean isActive;
    private String imageUrl; // Category icon/image URL
}
