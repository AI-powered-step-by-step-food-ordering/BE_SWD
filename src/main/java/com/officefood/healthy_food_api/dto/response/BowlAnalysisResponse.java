package com.officefood.healthy_food_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response phân tích bowl từ AI")
public class BowlAnalysisResponse {

    @Schema(description = "ID của bowl đã phân tích")
    private String bowlId;

    @Schema(description = "Tên bowl")
    private String bowlName;

    @Schema(description = "Mục tiêu sức khỏe của user")
    private String userGoal;

    @Schema(description = "Câu hỏi của user")
    private String userMessage;

    @Schema(description = "Phản hồi từ AI")
    private String aiResponse;

    @Schema(description = "Thời gian phân tích")
    private ZonedDateTime analyzedAt;

    @Schema(description = "Tổng calories ước tính (nếu có)")
    private Double estimatedCalories;

    @Schema(description = "Tổng protein ước tính (nếu có)")
    private Double estimatedProtein;
}

