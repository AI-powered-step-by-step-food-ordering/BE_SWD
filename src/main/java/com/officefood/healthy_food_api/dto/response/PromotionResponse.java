package com.officefood.healthy_food_api.dto.response;

import lombok.*;

@Data
public class PromotionResponse {
    private java.util.UUID id;
    private String code;
    private String name;
    private String type;
    private Double percentOff;
    private Double amountOff;
    private Double minOrderValue;
    private Boolean isActive;
}
