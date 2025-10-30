package com.officefood.healthy_food_api.model.enums;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,      // Chef is preparing the meal
    READY,          // Ready for pickup
    COMPLETED,      // Order completed/delivered
    CANCELLED       // Order cancelled
}
