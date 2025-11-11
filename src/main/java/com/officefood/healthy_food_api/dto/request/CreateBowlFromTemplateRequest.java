package com.officefood.healthy_food_api.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBowlFromTemplateRequest {
    @NotNull(message = "Order ID is required")
    private String orderId;

    @NotNull(message = "Template ID is required")
    private String templateId;

    /**
     * Custom name cho bowl (optional, nếu không có sẽ dùng tên template)
     */
    private String customName;

    /**
     * Custom instruction (optional)
     */
    private String instruction;
}

