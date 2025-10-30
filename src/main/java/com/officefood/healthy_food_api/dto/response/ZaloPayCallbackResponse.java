package com.officefood.healthy_food_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZaloPayCallbackResponse {
    private Integer returnCode;
    private String returnMessage;
}

