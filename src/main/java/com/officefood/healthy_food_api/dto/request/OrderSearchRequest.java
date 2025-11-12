package com.officefood.healthy_food_api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSearchRequest {

    // Exact match search - simplified to only essential fields
    private String userId;      // Find orders by user
    private String storeId;     // Find orders by store
}



