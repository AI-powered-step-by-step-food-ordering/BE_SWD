package com.officefood.healthy_food_api.dto.response;

import lombok.*;
import java.time.ZonedDateTime;

@Data
public class InventoryResponse {
    private java.util.UUID id;
    private java.util.UUID storeId;
    private java.util.UUID ingredientId;
    private String action;
    private Double quantityChange;
    private Double balanceAfter;
    private ZonedDateTime createdAt;
}
