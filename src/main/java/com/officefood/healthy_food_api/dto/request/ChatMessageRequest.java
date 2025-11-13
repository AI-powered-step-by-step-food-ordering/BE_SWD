package com.officefood.healthy_food_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request chat message đơn giản")
public class ChatMessageRequest {

    @NotBlank(message = "Message is required")
    @Size(min = 1, max = 2000, message = "Message must be between 1 and 2000 characters")
    @Schema(description = "Nội dung tin nhắn", example = "Hôm nay tôi nên ăn gì?")
    private String message;
}

