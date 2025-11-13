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
@Schema(description = "Request để phân tích bowl với AI")
public class BowlAnalysisRequest {

    @NotBlank(message = "Bowl ID is required")
    @Schema(description = "ID của bowl cần phân tích", example = "123e4567-e89b-12d3-a456-426614174000")
    private String bowlId;

    @NotBlank(message = "User message is required")
    @Size(min = 1, max = 1000, message = "Message must be between 1 and 1000 characters")
    @Schema(description = "Câu hỏi của người dùng", example = "Bowl này có phù hợp với mục tiêu giảm cân của tôi không?")
    private String userMessage;
}