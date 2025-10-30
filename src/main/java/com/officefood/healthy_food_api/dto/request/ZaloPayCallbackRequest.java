package com.officefood.healthy_food_api.dto.request;

import lombok.Data;

@Data
public class ZaloPayCallbackRequest {
    private String data;
    private String mac;
    private Integer type;
}

