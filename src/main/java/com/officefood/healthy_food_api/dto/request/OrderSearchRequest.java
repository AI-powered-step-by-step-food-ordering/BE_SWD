package com.officefood.healthy_food_api.dto.request;

import com.officefood.healthy_food_api.model.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSearchRequest {

    // Exact match search
    private String userId;      // Find orders by user ID
    private String storeId;     // Find orders by store ID

    // Partial match search (case-insensitive)
    private String fullName;    // Search by user's full name (partial match)

    // Enum search
    private OrderStatus status; // Find orders by status (PENDING, CONFIRMED, CANCELLED, COMPLETED)
}



