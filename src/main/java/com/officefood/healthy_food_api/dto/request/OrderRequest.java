package com.officefood.healthy_food_api.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
public class OrderRequest {
    private java.time.OffsetDateTime pickupAt;
    private String note;
    @NotNull private String storeId;
    @NotNull private String userId;
}
