package com.officefood.healthy_food_api.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
public class CategoryRequest {
    @NotBlank private String name;
    @NotBlank private String kind;
    private Integer displayOrder;
    private Boolean isActive;
    private String imageUrl; // Category icon/image URL
}
