package com.officefood.healthy_food_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FcmTokenRequest {
    @NotBlank(message = "FCM token is required")
    private String fcmToken;

    @NotBlank(message = "Platform is required (android/ios)")
    private String platform;

    @NotBlank(message = "Device ID is required")
    private String deviceId;
}
