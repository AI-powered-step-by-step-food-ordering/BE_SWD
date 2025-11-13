# Tính năng AI Bowl Analysis - Hướng dẫn triển khai

## Tổng quan

Tính năng này cho phép người dùng chat với AI (Google Gemini) để phân tích xem bowl họ vừa tạo có phù hợp với mục tiêu sức khỏe (goalCode) của họ hay không. AI sẽ phân tích dựa trên thông tin bowl, bowl items, ingredients và số lượng của từng item.

## Mục tiêu

- Người dùng có thể hỏi AI về bowl sau khi tạo trong order
- AI phân tích bowl dựa trên: ingredients, quantity, và goalCode của user (MUSCLE_GAIN, WEIGHT_LOSS, MAINTAIN_HEALTH, etc.)
- Cung cấp lời khuyên dinh dưỡng và đề xuất cải thiện

## Kiến trúc

### 1. Entities hiện có (đã phân tích)

- **User**: Chứa `goalCode` (String) - mục tiêu sức khỏe
- **Bowl**: Liên kết với Order, BowlTemplate, và chứa danh sách BowlItem
- **BowlItem**: Chứa Ingredient và quantity
- **Ingredient**: Thông tin nguyên liệu (name, category, unit, nutritional info)
- **Order**: Chứa danh sách Bowl và thông tin User

### 2. Thành phần cần triển khai

```
├── config/
│   └── GeminiConfig.java              # Cấu hình Gemini API
├── service/
│   ├── GeminiService.java             # Service gọi Gemini API
│   └── impl/
│       ├── GeminiServiceImpl.java     # Implementation
│       └── BowlAnalysisServiceImpl.java # Service phân tích bowl
├── controller/
│   └── BowlAnalysisController.java    # REST API endpoints
├── dto/
│   ├── request/
│   │   ├── BowlAnalysisRequest.java   # Request từ client
│   │   └── ChatMessageRequest.java    # Chat message
│   └── response/
│       ├── BowlAnalysisResponse.java  # Response phân tích
│       └── ChatMessageResponse.java   # Chat response
└── model/
    └── BowlAnalysisHistory.java       # Lưu lịch sử chat (optional)
```

## Bước 1: Thêm dependency Google Gemini

### File: `pom.xml`

```xml
<!-- Google Gemini AI SDK -->
<dependency>
    <groupId>com.google.genai</groupId>
    <artifactId>google-genai</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Lưu ý**: Sau khi thêm dependency, chạy:
```bash
mvn clean install
```

## Bước 2: Cấu hình Gemini API

### File: `src/main/resources/application.yml`

Thêm cấu hình sau:

```yaml
app:
  gemini:
    api-key: ${GEMINI_API_KEY:your-api-key-here}
    model: gemini-2.0-flash-exp
    temperature: 0.7
    max-tokens: 1000
    timeout: 30000
```

### File: `src/main/java/com/officefood/healthy_food_api/config/GeminiConfig.java`

```java
package com.officefood.healthy_food_api.config;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {
    
    @Value("${app.gemini.api-key}")
    private String apiKey;
    
    @Value("${app.gemini.model:gemini-2.0-flash-exp}")
    private String model;
    
    @Value("${app.gemini.temperature:0.7}")
    private Double temperature;
    
    @Value("${app.gemini.max-tokens:1000}")
    private Integer maxTokens;
    
    @Bean
    public Client geminiClient() {
        // Nếu API key được set qua environment variable GEMINI_API_KEY
        // thì Client sẽ tự động sử dụng
        return new Client();
    }
    
    public String getModel() {
        return model;
    }
    
    public Double getTemperature() {
        return temperature;
    }
    
    public Integer getMaxTokens() {
        return maxTokens;
    }
}
```

## Bước 3: Tạo DTOs

### File: `src/main/java/com/officefood/healthy_food_api/dto/request/BowlAnalysisRequest.java`

```java
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
```

### File: `src/main/java/com/officefood/healthy_food_api/dto/response/BowlAnalysisResponse.java`

```java
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
```

### File: `src/main/java/com/officefood/healthy_food_api/dto/request/ChatMessageRequest.java`

```java
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
    @Size(min = 1, max = 2000)
    @Schema(description = "Nội dung tin nhắn", example = "Hôm nay tôi nên ăn gì?")
    private String message;
}
```

## Bước 4: Tạo Service Layer

### File: `src/main/java/com/officefood/healthy_food_api/service/GeminiService.java`

```java
package com.officefood.healthy_food_api.service;

public interface GeminiService {
    
    /**
     * Gửi prompt đơn giản đến Gemini và nhận response
     */
    String generateContent(String prompt);
    
    /**
     * Phân tích bowl dựa trên thông tin bowl và mục tiêu của user
     */
    String analyzeBowl(String bowlId, String userMessage);
    
    /**
     * Chat đơn giản với Gemini (không context bowl)
     */
    String chat(String message);
}
```

### File: `src/main/java/com/officefood/healthy_food_api/service/impl/GeminiServiceImpl.java`

```java
package com.officefood.healthy_food_api.service.impl;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.officefood.healthy_food_api.config.GeminiConfig;
import com.officefood.healthy_food_api.exception.ResourceNotFoundException;
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
            log.info("Sending prompt to Gemini API: {}", prompt);
            
            GenerateContentResponse response = geminiClient.models.generateContent(
                geminiConfig.getModel(),
                prompt,
                null
            );
            
            String responseText = response.text();
            log.info("Received response from Gemini API");
            
            return responseText;
        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate content from Gemini API: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public String analyzeBowl(String bowlId, String userMessage) {
        // Lấy bowl với tất cả thông tin cần thiết
        Bowl bowl = bowlRepository.findById(bowlId)
            .orElseThrow(() -> new ResourceNotFoundException("Bowl not found with id: " + bowlId));
        
        // Lấy user và goal
        User user = bowl.getOrder().getUser();
        String goalCode = user.getGoalCode() != null ? user.getGoalCode() : "GENERAL_HEALTH";
        
        // Build prompt với thông tin chi tiết
        String prompt = buildBowlAnalysisPrompt(bowl, goalCode, userMessage);
        
        // Gọi Gemini API
        return generateContent(prompt);
    }
    
    @Override
    public String chat(String message) {
        String prompt = "Bạn là một chuyên gia dinh dưỡng AI. " +
                       "Hãy trả lời câu hỏi sau một cách chuyên nghiệp và hữu ích:\n\n" +
                       message;
        return generateContent(prompt);
    }
    
    /**
     * Xây dựng prompt chi tiết cho việc phân tích bowl
     */
    private String buildBowlAnalysisPrompt(Bowl bowl, String goalCode, String userMessage) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Bạn là một chuyên gia dinh dưỡng AI chuyên nghiệp.\n\n");
        prompt.append("MỤC TIÊU SỨC KHỎE CỦA NGƯỜI DÙNG: ").append(translateGoalCode(goalCode)).append("\n\n");
        
        prompt.append("THÔNG TIN BOWL:\n");
        prompt.append("- Tên: ").append(bowl.getName()).append("\n");
        if (bowl.getInstruction() != null && !bowl.getInstruction().isEmpty()) {
            prompt.append("- Hướng dẫn: ").append(bowl.getInstruction()).append("\n");
        }
        prompt.append("- Giá: ").append(bowl.getLinePrice()).append(" VNĐ\n\n");
        
        prompt.append("THÀNH PHẦN NGUYÊN LIỆU:\n");
        int index = 1;
        for (BowlItem item : bowl.getItems()) {
            Ingredient ingredient = item.getIngredient();
            prompt.append(index++).append(". ")
                  .append(ingredient.getName())
                  .append(" - Số lượng: ").append(item.getQuantity())
                  .append(" ").append(ingredient.getUnit() != null ? ingredient.getUnit() : "đơn vị")
                  .append("\n");
        }
        
        prompt.append("\nCÂU HỎI CỦA NGƯỜI DÙNG:\n");
        prompt.append(userMessage).append("\n\n");
        
        prompt.append("YÊU CẦU:\n");
        prompt.append("1. Phân tích bowl này có phù hợp với mục tiêu sức khỏe không\n");
        prompt.append("2. Đánh giá dinh dưỡng (calories, protein, carbs, fats ước tính nếu có thể)\n");
        prompt.append("3. Đưa ra lời khuyên cải thiện cụ thể\n");
        prompt.append("4. Trả lời câu hỏi của người dùng một cách chi tiết\n");
        prompt.append("5. Sử dụng tiếng Việt, ngắn gọn, dễ hiểu\n");
        
        return prompt.toString();
    }
    
    /**
     * Chuyển đổi goal code thành mô tả dễ hiểu
     */
    private String translateGoalCode(String goalCode) {
        return switch (goalCode.toUpperCase()) {
            case "MUSCLE_GAIN", "GAIN_MUSCLE" -> "Tăng cơ (Muscle Gain)";
            case "WEIGHT_LOSS", "LOSE_WEIGHT" -> "Giảm cân (Weight Loss)";
            case "MAINTAIN_HEALTH", "MAINTAIN" -> "Duy trì sức khỏe (Maintain Health)";
            case "GAIN_WEIGHT" -> "Tăng cân (Gain Weight)";
            case "IMPROVE_ENDURANCE" -> "Tăng sức bền (Improve Endurance)";
            default -> goalCode;
        };
    }
}
```

## Bước 5: Tạo Repository (nếu cần)

### File: `src/main/java/com/officefood/healthy_food_api/repository/BowlRepository.java`

Kiểm tra xem repository đã có phương thức cần thiết chưa. Nếu chưa, thêm:

```java
package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.Bowl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BowlRepository extends JpaRepository<Bowl, String> {
    
    @Query("SELECT b FROM Bowl b " +
           "LEFT JOIN FETCH b.items bi " +
           "LEFT JOIN FETCH bi.ingredient i " +
           "LEFT JOIN FETCH i.category " +
           "LEFT JOIN FETCH b.order o " +
           "LEFT JOIN FETCH o.user " +
           "WHERE b.id = :bowlId")
    Optional<Bowl> findByIdWithDetails(@Param("bowlId") String bowlId);
}
```

**Lưu ý**: Cập nhật `GeminiServiceImpl.java` để sử dụng `findByIdWithDetails` thay vì `findById` để tránh N+1 query problem.

## Bước 6: Tạo Controller

### File: `src/main/java/com/officefood/healthy_food_api/controller/BowlAnalysisController.java`

```java
package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.dto.request.BowlAnalysisRequest;
import com.officefood.healthy_food_api.dto.request.ChatMessageRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.BowlAnalysisResponse;
import com.officefood.healthy_food_api.exception.ResourceNotFoundException;
import com.officefood.healthy_food_api.model.Bowl;
import com.officefood.healthy_food_api.repository.BowlRepository;
import com.officefood.healthy_food_api.service.GeminiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;

@RestController
@RequestMapping("/api/bowl-analysis")
@RequiredArgsConstructor
@Tag(name = "Bowl Analysis AI", description = "AI-powered bowl analysis endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class BowlAnalysisController {
    
    private final GeminiService geminiService;
    private final BowlRepository bowlRepository;
    
    @PostMapping("/analyze")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    @Operation(summary = "Phân tích bowl với AI", 
               description = "Gửi thông tin bowl và câu hỏi đến AI để phân tích dinh dưỡng")
    public ResponseEntity<ApiResponse<BowlAnalysisResponse>> analyzeBowl(
            @Valid @RequestBody BowlAnalysisRequest request) {
        
        // Lấy thông tin bowl
        Bowl bowl = bowlRepository.findById(request.getBowlId())
            .orElseThrow(() -> new ResourceNotFoundException("Bowl not found with id: " + request.getBowlId()));
        
        // Gọi AI để phân tích
        String aiResponse = geminiService.analyzeBowl(request.getBowlId(), request.getUserMessage());
        
        // Build response
        BowlAnalysisResponse response = BowlAnalysisResponse.builder()
            .bowlId(bowl.getId())
            .bowlName(bowl.getName())
            .userGoal(bowl.getOrder().getUser().getGoalCode())
            .userMessage(request.getUserMessage())
            .aiResponse(aiResponse)
            .analyzedAt(ZonedDateTime.now())
            .build();
        
        return ResponseEntity.ok(ApiResponse.success(200, "Bowl analyzed successfully", response));
    }
    
    @PostMapping("/chat")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    @Operation(summary = "Chat với AI dinh dưỡng",
               description = "Chat đơn giản với AI về dinh dưỡng và sức khỏe")
    public ResponseEntity<ApiResponse<String>> chat(@Valid @RequestBody ChatMessageRequest request) {
        
        String aiResponse = geminiService.chat(request.getMessage());
        
        return ResponseEntity.ok(ApiResponse.success(200, "Chat response generated", aiResponse));
    }
    
    @GetMapping("/test")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    @Operation(summary = "Test Gemini API connection", 
               description = "Kiểm tra kết nối đến Gemini API")
    public ResponseEntity<ApiResponse<String>> testGeminiConnection() {
        
        String testResponse = geminiService.generateContent(
            "Hello! Please respond with 'Gemini API is working correctly!' in Vietnamese."
        );
        
        return ResponseEntity.ok(ApiResponse.success(200, "Gemini API test successful", testResponse));
    }
}
```

## Bước 7: Cập nhật Security Config (nếu cần)

Đảm bảo các endpoints mới được cấu hình đúng trong `SecurityConfig.java`:

```java
// Trong SecurityConfig.java
http.authorizeHttpRequests(auth -> auth
    // ... existing config ...
    .requestMatchers("/api/bowl-analysis/**").authenticated()
    // ... rest of config ...
);
```

## Bước 8: Testing

### 8.1. Setup môi trường

1. Đăng ký tài khoản Google AI Studio: https://aistudio.google.com/
2. Tạo API Key trong Google AI Studio
3. Thêm API Key vào environment variables:

**Windows (CMD):**
```cmd
set GEMINI_API_KEY=your-actual-api-key-here
```

**Windows (PowerShell):**
```powershell
$env:GEMINI_API_KEY="your-actual-api-key-here"
```

**Linux/Mac:**
```bash
export GEMINI_API_KEY=your-actual-api-key-here
```

Hoặc thêm trực tiếp vào `application.yml`:
```yaml
app:
  gemini:
    api-key: AIzaSy...your-actual-key...
```

### 8.2. Test endpoints

**1. Test Gemini Connection:**
```bash
GET http://localhost:4458/api/bowl-analysis/test
Authorization: Bearer <your-jwt-token>
```

**2. Analyze Bowl:**
```bash
POST http://localhost:4458/api/bowl-analysis/analyze
Authorization: Bearer <your-jwt-token>
Content-Type: application/json

{
  "bowlId": "bowl-uuid-here",
  "userMessage": "Bowl này có phù hợp với mục tiêu giảm cân của tôi không?"
}
```

**3. Simple Chat:**
```bash
POST http://localhost:4458/api/bowl-analysis/chat
Authorization: Bearer <your-jwt-token>
Content-Type: application/json

{
  "message": "Tôi nên ăn gì để tăng cơ?"
}
```

### 8.3. Postman Collection

Tạo file `Bowl_Analysis_AI.postman_collection.json`:

```json
{
  "info": {
    "name": "Bowl Analysis AI",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Test Gemini Connection",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{jwt_token}}",
            "type": "text"
          }
        ],
        "url": {
          "raw": "{{base_url}}/api/bowl-analysis/test",
          "host": ["{{base_url}}"],
          "path": ["api", "bowl-analysis", "test"]
        }
      }
    },
    {
      "name": "Analyze Bowl",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{jwt_token}}",
            "type": "text"
          },
          {
            "key": "Content-Type",
            "value": "application/json",
            "type": "text"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"bowlId\": \"{{bowl_id}}\",\n  \"userMessage\": \"Bowl này có phù hợp với mục tiêu giảm cân của tôi không?\"\n}"
        },
        "url": {
          "raw": "{{base_url}}/api/bowl-analysis/analyze",
          "host": ["{{base_url}}"],
          "path": ["api", "bowl-analysis", "analyze"]
        }
      }
    },
    {
      "name": "Chat with AI",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{jwt_token}}",
            "type": "text"
          },
          {
            "key": "Content-Type",
            "value": "application/json",
            "type": "text"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"message\": \"Tôi nên ăn gì để tăng cơ?\"\n}"
        },
        "url": {
          "raw": "{{base_url}}/api/bowl-analysis/chat",
          "host": ["{{base_url}}"],
          "path": ["api", "bowl-analysis", "chat"]
        }
      }
    }
  ],
  "variable": [
    {
      "key": "base_url",
      "value": "http://localhost:4458"
    },
    {
      "key": "jwt_token",
      "value": ""
    },
    {
      "key": "bowl_id",
      "value": ""
    }
  ]
}
```

## Bước 9: Tính năng nâng cao (Optional)

### 9.1. Lưu lịch sử chat

Tạo entity `BowlAnalysisHistory` để lưu lịch sử phân tích:

```java
@Entity
@Table(name = "bowl_analysis_history")
public class BowlAnalysisHistory extends BaseEntity {
    @Id @GeneratedValue @UuidGenerator
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "bowl_id")
    private Bowl bowl;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(length = 2000)
    private String userMessage;
    
    @Column(columnDefinition = "TEXT")
    private String aiResponse;
    
    private ZonedDateTime analyzedAt;
}
```

### 9.2. Rate limiting

Thêm rate limiting để tránh lạm dụng API:

```java
@RateLimiter(name = "gemini-api")
public String analyzeBowl(String bowlId, String userMessage) {
    // ... implementation
}
```

### 9.3. Caching

Cache kết quả phân tích cho các câu hỏi giống nhau:

```java
@Cacheable(value = "bowl-analysis", key = "#bowlId + '-' + #userMessage")
public String analyzeBowl(String bowlId, String userMessage) {
    // ... implementation
}
```

## Bước 10: Error Handling

Thêm custom exceptions:

```java
public class GeminiApiException extends RuntimeException {
    public GeminiApiException(String message) {
        super(message);
    }
    
    public GeminiApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

Xử lý trong `@ControllerAdvice`:

```java
@ExceptionHandler(GeminiApiException.class)
public ResponseEntity<ApiResponse<Void>> handleGeminiApiException(GeminiApiException ex) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(ApiResponse.error(503, "GEMINI_API_ERROR", ex.getMessage()));
}
```

## Bước 11: Documentation

Thêm Swagger annotations chi tiết:

```java
@Operation(
    summary = "Phân tích bowl với AI",
    description = "Endpoint này cho phép người dùng gửi bowl và câu hỏi đến AI Gemini để nhận phân tích dinh dưỡng chi tiết",
    responses = {
        @ApiResponse(responseCode = "200", description = "Phân tích thành công"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy bowl"),
        @ApiResponse(responseCode = "503", description = "Lỗi kết nối Gemini API")
    }
)
```

## Checklist triển khai

- [ ] Thêm dependency `google-genai` vào `pom.xml`
- [ ] Chạy `mvn clean install`
- [ ] Tạo `GeminiConfig.java`
- [ ] Cập nhật `application.yml` với cấu hình Gemini
- [ ] Tạo DTOs (Request & Response)
- [ ] Tạo `GeminiService` interface
- [ ] Implement `GeminiServiceImpl`
- [ ] Cập nhật `BowlRepository` (nếu cần)
- [ ] Tạo `BowlAnalysisController`
- [ ] Cập nhật `SecurityConfig` (nếu cần)
- [ ] Lấy API Key từ Google AI Studio
- [ ] Set environment variable `GEMINI_API_KEY`
- [ ] Test endpoints với Postman
- [ ] Viết unit tests
- [ ] Cập nhật documentation

## Best Practices

1. **Security**: 
   - Không commit API key vào Git
   - Sử dụng environment variables
   - Implement rate limiting

2. **Performance**:
   - Cache kết quả khi có thể
   - Sử dụng async processing cho requests lâu
   - Optimize database queries với JOIN FETCH

3. **User Experience**:
   - Cung cấp error messages rõ ràng
   - Validate input trước khi gọi API
   - Timeout hợp lý (30s)

4. **Monitoring**:
   - Log tất cả requests/responses
   - Track API usage và costs
   - Monitor response times

## Troubleshooting

### Lỗi thường gặp:

1. **401 Unauthorized**: API key không hợp lệ hoặc chưa được set
2. **429 Too Many Requests**: Vượt quá quota, cần implement rate limiting
3. **500 Internal Server Error**: Kiểm tra logs, có thể do network hoặc API down
4. **N+1 Query Problem**: Sử dụng JOIN FETCH trong repository queries

## Tài liệu tham khảo

- [Google Gemini API Documentation](https://ai.google.dev/docs)
- [Google GenAI Java SDK](https://github.com/google/generative-ai-java)
- [Spring Boot Best Practices](https://spring.io/guides)
- [REST API Design](https://restfulapi.net/)

## Kết luận

Tính năng AI Bowl Analysis giúp người dùng:
- Hiểu rõ hơn về dinh dưỡng trong bowl của họ
- Nhận lời khuyên từ AI dựa trên mục tiêu sức khỏe
- Tối ưu hóa lựa chọn thực phẩm

Hệ thống có thể mở rộng thêm:
- Phân tích đa ngôn ngữ
- Tích hợp với fitness tracking
- Personalized meal planning
- Dietary restrictions và allergies detection

---

**Người soạn**: AI Assistant  
**Ngày tạo**: 2025-01-13  
**Phiên bản**: 1.0

