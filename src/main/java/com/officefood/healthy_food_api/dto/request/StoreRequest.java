package com.officefood.healthy_food_api.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
public class StoreRequest {
    @NotBlank private String name;
    private String address;
    private String phone;
    private String timezone;
}
