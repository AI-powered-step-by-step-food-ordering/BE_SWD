package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.dto.request.BowlAnalysisRequest;
import com.officefood.healthy_food_api.dto.request.ChatMessageRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.BowlAnalysisResponse;
import com.officefood.healthy_food_api.exception.NotFoundException;
import com.officefood.healthy_food_api.model.Bowl;
import com.officefood.healthy_food_api.repository.BowlRepository;
import com.officefood.healthy_food_api.service.GeminiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;

@RestController
@RequestMapping("/api/bowl-analysis")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bowl Analysis AI", description = "AI-powered bowl analysis endpoints using Google Gemini")
@SecurityRequirement(name = "Bearer Authentication")
public class BowlAnalysisController {

    private final GeminiService geminiService;
    private final BowlRepository bowlRepository;

    @PostMapping("/analyze")
    @Operation(
        summary = "Phân tích bowl với AI",
        description = "Gửi thông tin bowl và câu hỏi đến AI Gemini để phân tích dinh dưỡng dựa trên mục tiêu sức khỏe của người dùng"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bowl analyzed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Bowl not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "AI service error")
    })
    public ResponseEntity<ApiResponse<BowlAnalysisResponse>> analyzeBowl(
            @Valid @RequestBody BowlAnalysisRequest request) {

        log.info("Analyzing bowl with ID: {} for user message: {}",
                 request.getBowlId(),
                 request.getUserMessage().substring(0, Math.min(50, request.getUserMessage().length())));

        // Lấy thông tin bowl để validate
        Bowl bowl = bowlRepository.findById(request.getBowlId())
            .orElseThrow(() -> new NotFoundException("Bowl not found with id: " + request.getBowlId()));

        // Gọi AI để phân tích
        String aiResponse = geminiService.analyzeBowl(request.getBowlId(), request.getUserMessage());

        // Build response
        BowlAnalysisResponse response = BowlAnalysisResponse.builder()
            .bowlId(bowl.getId())
            .bowlName(bowl.getName())
            .userGoal(bowl.getOrder() != null && bowl.getOrder().getUser() != null
                      ? bowl.getOrder().getUser().getGoalCode()
                      : null)
            .userMessage(request.getUserMessage())
            .aiResponse(aiResponse)
            .analyzedAt(ZonedDateTime.now())
            .build();

        log.info("Bowl analysis completed successfully for bowl: {}", request.getBowlId());

        return ResponseEntity.ok(ApiResponse.success(200, "Bowl analyzed successfully", response));
    }

    @PostMapping("/chat")
    @Operation(
        summary = "Chat với AI dinh dưỡng",
        description = "Chat đơn giản với AI Gemini về dinh dưỡng và sức khỏe, không cần context bowl cụ thể"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Chat response generated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "AI service error")
    })
    public ResponseEntity<ApiResponse<String>> chat(@Valid @RequestBody ChatMessageRequest request) {

        log.info("Processing chat message: {}",
                 request.getMessage().substring(0, Math.min(50, request.getMessage().length())));

        String aiResponse = geminiService.chat(request.getMessage());

        log.info("Chat response generated successfully");

        return ResponseEntity.ok(ApiResponse.success(200, "Chat response generated", aiResponse));
    }

    @GetMapping("/test")
    @Operation(
        summary = "Test Gemini API connection",
        description = "Kiểm tra kết nối đến Gemini API để đảm bảo service hoạt động bình thường"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Gemini API is working"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Gemini API connection failed")
    })
    public ResponseEntity<ApiResponse<String>> testGeminiConnection() {

        log.info("Testing Gemini API connection...");

        try {
            String testResponse = geminiService.generateContent(
                "Xin chào! Vui lòng phản hồi bằng tiếng Việt: 'Gemini API đang hoạt động tốt!'"
            );

            log.info("Gemini API test successful");

            return ResponseEntity.ok(ApiResponse.success(
                200,
                "Gemini API connection successful",
                testResponse
            ));
        } catch (Exception e) {
            log.error("Gemini API test failed: {}", e.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error(
                500,
                "GEMINI_API_ERROR",
                "Failed to connect to Gemini API: " + e.getMessage()
            ));
        }
    }
}
