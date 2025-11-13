package com.officefood.healthy_food_api.service.impl;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.officefood.healthy_food_api.config.GeminiConfig;
import com.officefood.healthy_food_api.exception.NotFoundException;
import com.officefood.healthy_food_api.model.Bowl;
import com.officefood.healthy_food_api.model.BowlItem;
import com.officefood.healthy_food_api.model.Ingredient;
import com.officefood.healthy_food_api.model.User;
import com.officefood.healthy_food_api.repository.BowlRepository;
import com.officefood.healthy_food_api.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiServiceImpl implements GeminiService {

    private final Client geminiClient;
    private final GeminiConfig geminiConfig;
    private final BowlRepository bowlRepository;

    @Override
    public String generateContent(String prompt) {
        try {
            log.info("Sending prompt to Gemini API: {}", prompt.substring(0, Math.min(100, prompt.length())));

            GenerateContentResponse response = geminiClient.models.generateContent(
                geminiConfig.getModel(),
                prompt,
                null
            );

            String responseText = response.text();
            log.info("Received response from Gemini API (length: {} chars)", responseText.length());

            return responseText;
        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate content from Gemini API: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String analyzeBowl(String bowlId, String userMessage) {
        // Lấy bowl với tất cả thông tin cần thiết
        Bowl bowl = bowlRepository.findByIdWithDetailsForAI(bowlId)
            .orElseThrow(() -> new NotFoundException("Bowl not found with id: " + bowlId));

        // Lấy user và goal
        User user = bowl.getOrder().getUser();
        String goalCode = user.getGoalCode() != null ? user.getGoalCode() : "GENERAL_HEALTH";

        // Build prompt với thông tin chi tiết
        String prompt = buildBowlAnalysisPrompt(bowl, goalCode, userMessage);

        log.info("Analyzing bowl {} for user goal: {}", bowlId, goalCode);

        // Gọi Gemini API
        return generateContent(prompt);
    }

    @Override
    public String chat(String message) {
        String prompt = "Bạn là một chuyên gia dinh dưỡng AI chuyên nghiệp và thân thiện. " +
                       "Hãy trả lời câu hỏi sau một cách chi tiết, chính xác và hữu ích. " +
                       "Sử dụng tiếng Việt và đưa ra lời khuyên thực tế:\n\n" +
                       message;
        return generateContent(prompt);
    }

    /**
     * Xây dựng prompt chi tiết cho việc phân tích bowl
     */
    private String buildBowlAnalysisPrompt(Bowl bowl, String goalCode, String userMessage) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Bạn là một chuyên gia dinh dưỡng AI chuyên nghiệp tại Việt Nam.\n\n");

        prompt.append("=== MỤC TIÊU SỨC KHỎE CỦA NGƯỜI DÙNG ===\n");
        prompt.append(translateGoalCode(goalCode)).append("\n\n");

        prompt.append("=== THÔNG TIN BOWL ===\n");
        prompt.append("Tên: ").append(bowl.getName() != null ? bowl.getName() : "Chưa đặt tên").append("\n");

        if (bowl.getInstruction() != null && !bowl.getInstruction().isEmpty()) {
            prompt.append("Hướng dẫn: ").append(bowl.getInstruction()).append("\n");
        }

        if (bowl.getLinePrice() != null) {
            prompt.append("Giá: ").append(String.format("%,.0f", bowl.getLinePrice())).append(" VNĐ\n");
        }

        prompt.append("\n=== THÀNH PHẦN NGUYÊN LIỆU ===\n");

        if (bowl.getItems() != null && !bowl.getItems().isEmpty()) {
            int index = 1;
            for (BowlItem item : bowl.getItems()) {
                Ingredient ingredient = item.getIngredient();
                prompt.append(index++).append(". ")
                      .append(ingredient.getName())
                      .append("\n   - Số lượng: ").append(item.getQuantity() != null ? item.getQuantity() : 0)
                      .append(" ").append(ingredient.getUnit() != null ? ingredient.getUnit() : "đơn vị");

                if (ingredient.getCategory() != null && ingredient.getCategory().getName() != null) {
                    prompt.append("\n   - Loại: ").append(ingredient.getCategory().getName());
                }

                if (item.getUnitPrice() != null) {
                    prompt.append("\n   - Giá: ").append(String.format("%,.0f", item.getUnitPrice())).append(" VNĐ");
                }

                prompt.append("\n");
            }
        } else {
            prompt.append("(Chưa có nguyên liệu)\n");
        }

        prompt.append("\n=== CÂU HỎI CỦA NGƯỜI DÙNG ===\n");
        prompt.append(userMessage).append("\n\n");

        prompt.append("=== YÊU CẦU PHÂN TÍCH ===\n");
        prompt.append("Hãy phân tích chi tiết và cung cấp:\n\n");
        prompt.append("1. **Đánh giá chung**: Bowl này có phù hợp với mục tiêu ").append(translateGoalCode(goalCode)).append(" không?\n\n");
        prompt.append("2. **Phân tích dinh dưỡng**: \n");
        prompt.append("   - Ước tính calories tổng\n");
        prompt.append("   - Ước tính protein, carbs, fats\n");
        prompt.append("   - Vitamin và khoáng chất chính\n\n");
        prompt.append("3. **Điểm mạnh**: Những gì tốt về bowl này\n\n");
        prompt.append("4. **Điểm cần cải thiện**: Những gì có thể làm tốt hơn\n\n");
        prompt.append("5. **Lời khuyên cụ thể**: \n");
        prompt.append("   - Nên thêm/bớt nguyên liệu gì?\n");
        prompt.append("   - Nên ăn vào thời điểm nào trong ngày?\n");
        prompt.append("   - Kết hợp với thực đơn khác như thế nào?\n\n");
        prompt.append("6. **Trả lời câu hỏi**: Trả lời trực tiếp câu hỏi của người dùng\n\n");
        prompt.append("Hãy viết bằng tiếng Việt, ngắn gọn nhưng đầy đủ thông tin, dễ hiểu và thân thiện.\n");
        prompt.append("Sử dụng markdown để định dạng (**, ##, -, v.v.) cho dễ đọc.\n");

        return prompt.toString();
    }

    /**
     * Chuyển đổi goal code thành mô tả dễ hiểu
     */
    private String translateGoalCode(String goalCode) {
        if (goalCode == null) {
            return "Duy trì sức khỏe tổng quát";
        }

        return switch (goalCode.toUpperCase()) {
            case "MUSCLE_GAIN", "GAIN_MUSCLE" -> "Tăng cơ (Muscle Gain)";
            case "WEIGHT_LOSS", "LOSE_WEIGHT" -> "Giảm cân (Weight Loss)";
            case "MAINTAIN_HEALTH", "MAINTAIN" -> "Duy trì sức khỏe (Maintain Health)";
            case "GAIN_WEIGHT" -> "Tăng cân (Gain Weight)";
            case "IMPROVE_ENDURANCE" -> "Tăng sức bền (Improve Endurance)";
            case "GENERAL_HEALTH" -> "Sức khỏe tổng quát (General Health)";
            case "BUILD_STRENGTH" -> "Tăng sức mạnh (Build Strength)";
            case "FLEXIBILITY" -> "Tăng độ dẻo dai (Flexibility)";
            default -> goalCode + " (Mục tiêu đặc biệt)";
        };
    }
}

