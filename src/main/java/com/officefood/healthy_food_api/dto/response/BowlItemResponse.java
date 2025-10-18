package com.officefood.healthy_food_api.dto.response;

import lombok.*;

@Data
public class BowlItemResponse {
    private java.util.UUID id;
    private java.util.UUID bowlId;
    private java.util.UUID ingredientId;
    private Double quantity;
    private Double unitPrice;
}
