package com.officefood.healthy_food_api.dto.response;

import lombok.*;
import java.time.ZonedDateTime;

@Data
public class InventoryResponse {
    private String id;
    private String storeId;
    private String ingredientId;
    private String action;
    private Double quantityChange;
    private Double balanceAfter;
    private ZonedDateTime createdAt;
}
