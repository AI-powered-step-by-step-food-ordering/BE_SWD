package com.officefood.healthy_food_api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreSearchRequest {

    // Exact match search
    private String storeId;

    // Partial match search (case-insensitive) - MAIN SEARCH FIELDS
    private String name;
    private String address;
    private String phone;
}

