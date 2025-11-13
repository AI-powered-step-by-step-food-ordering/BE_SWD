# 🧪 HƯỚNG DẪN TEST CHỨC NĂNG AI BOWL ANALYSIS

## Phần 1: Chuẩn bị

### 1.1. Chạy ứng dụng
```bash
mvn spring-boot:run
```

**Chờ đến khi thấy log:**
```
Started HealthyFoodApiApplication in X.XXX seconds
```

### 1.2. URL Base
```
http://localhost:4458
```

---

## Phần 2: Lấy JWT Token (Cần thiết cho tất cả các test)

### Bước 1: Login để lấy token

**Request:**
```http
POST http://localhost:4458/api/auth/login
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "admin123"
}
```

**Hoặc nếu có user khác:**
```json
{
  "email": "user@example.com", 
  "password": "password123"
}
```

**Response mẫu:**
```json
{
  "status": 200,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "...",
    "user": {
      "id": "user-uuid",
      "email": "admin@example.com",
      "fullName": "Admin User"
    }
  }
}
```

**✅ Copy giá trị `accessToken` để dùng cho các bước sau!**

---

## Phần 3: Test Gemini API Connection

### Test 1: Kiểm tra kết nối Gemini API

**Request:**
```http
GET http://localhost:4458/api/bowl-analysis/test
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response thành công:**
```json
{
  "status": 200,
  "message": "Gemini API connection successful",
  "data": "Gemini API đang hoạt động tốt!"
}
```

**❌ Nếu lỗi 401:**
- Kiểm tra JWT token có đúng không
- Token có hết hạn chưa (login lại)

**❌ Nếu lỗi 500 (Gemini API error):**
- Kiểm tra API key có đúng không
- Kiểm tra internet connection

---

## Phần 4: Lấy Bowl ID để test

### Bước 1: Lấy danh sách orders

**Request:**
```http
GET http://localhost:4458/api/orders/getall?page=0&size=10
Authorization: Bearer {your-token}
```

### Bước 2: Chọn một order và lấy bowl ID

**Response mẫu:**
```json
{
  "data": {
    "content": [
      {
        "id": "order-uuid-1",
        "bowls": [
          {
            "id": "bowl-uuid-abc123",  // ← Copy ID này
            "name": "Healthy Salad Bowl",
            "items": [...]
          }
        ]
      }
    ]
  }
}
```

**✅ Copy `bowl.id` để dùng cho test tiếp theo**

**HOẶC tạo bowl mới:**

```http
POST http://localhost:4458/api/orders
Authorization: Bearer {your-token}
Content-Type: application/json

{
  "storeId": "store-uuid",
  "pickupAt": "2025-11-14T12:00:00Z",
  "bowls": [
    {
      "templateId": "template-uuid",
      "name": "My Test Bowl",
      "items": [
        {
          "ingredientId": "ingredient-1",
          "quantity": 100
        }
      ]
    }
  ]
}
```

---

## Phần 5: Test Phân tích Bowl

### Test 2: Phân tích Bowl - Câu hỏi chung

**Request:**
```http
POST http://localhost:4458/api/bowl-analysis/analyze
Authorization: Bearer {your-token}
Content-Type: application/json

{
  "bowlId": "bowl-uuid-abc123",
  "userMessage": "Bowl này có tốt cho sức khỏe không?"
}
```

**Response mẫu:**
```json
{
  "status": 200,
  "message": "Bowl analyzed successfully",
  "data": {
    "bowlId": "bowl-uuid-abc123",
    "bowlName": "Healthy Salad Bowl",
    "userGoal": "WEIGHT_LOSS",
    "userMessage": "Bowl này có tốt cho sức khỏe không?",
    "aiResponse": "## Đánh giá chung\n\nBowl này rất phù hợp...",
    "analyzedAt": "2025-11-13T12:00:00Z"
  }
}
```

### Test 3: Phân tích Bowl - Hỏi về giảm cân

**Request:**
```json
{
  "bowlId": "bowl-uuid-abc123",
  "userMessage": "Bowl này có phù hợp với mục tiêu giảm cân của tôi không? Tôi nên điều chỉnh gì?"
}
```

### Test 4: Phân tích Bowl - Hỏi về dinh dưỡng

**Request:**
```json
{
  "bowlId": "bowl-uuid-abc123",
  "userMessage": "Cho tôi biết thông tin dinh dưỡng chi tiết: calories, protein, carbs, fats?"
}
```

### Test 5: Phân tích Bowl - Hỏi về thời điểm ăn

**Request:**
```json
{
  "bowlId": "bowl-uuid-abc123",
  "userMessage": "Bowl này nên ăn vào thời điểm nào trong ngày? Sáng, trưa hay tối?"
}
```

### Test 6: Phân tích Bowl - Hỏi về tăng cơ

**Request:**
```json
{
  "bowlId": "bowl-uuid-abc123",
  "userMessage": "Bowl này có đủ protein để tăng cơ không? Tôi có nên thêm gì không?"
}
```

---

## Phần 6: Test Chat Đơn Giản (Không cần Bowl)

### Test 7: Chat - Câu hỏi chung về dinh dưỡng

**Request:**
```http
POST http://localhost:4458/api/bowl-analysis/chat
Authorization: Bearer {your-token}
Content-Type: application/json

{
  "message": "Tôi nên ăn gì để tăng cơ hiệu quả?"
}
```

**Response:**
```json
{
  "status": 200,
  "message": "Chat response generated",
  "data": "Để tăng cơ hiệu quả, bạn nên:\n\n1. **Tăng lượng protein**..."
}
```

### Test 8: Chat - Lời khuyên giảm cân

**Request:**
```json
{
  "message": "Để giảm cân hiệu quả tôi nên ăn những gì và tránh những gì?"
}
```

### Test 9: Chat - Nguồn protein

**Request:**
```json
{
  "message": "Những nguồn protein tốt nhất cho người tập gym là gì?"
}
```

### Test 10: Chat - Thực đơn healthy

**Request:**
```json
{
  "message": "Gợi ý cho tôi một thực đơn healthy cho cả tuần?"
}
```

---

## Phần 7: Test với Postman

### Bước 1: Import Collection

1. Mở Postman
2. Click **Import**
3. Chọn file: `Bowl_Analysis_AI.postman_collection.json`
4. Click **Import**

### Bước 2: Set Variables

1. Click vào Collection
2. Tab **Variables**
3. Set các giá trị:
   - `base_url`: `http://localhost:4458`
   - `jwt_token`: `{paste-your-token-here}`
   - `bowl_id`: `{paste-bowl-id-here}`

### Bước 3: Chạy Tests

1. **Test Gemini Connection** - Chạy đầu tiên
2. **Analyze Bowl** - Thử các kịch bản khác nhau
3. **Chat** - Test chat đơn giản

---

## Phần 8: Test với cURL

### Test Gemini Connection
```bash
curl -X GET http://localhost:4458/api/bowl-analysis/test \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Test Analyze Bowl
```bash
curl -X POST http://localhost:4458/api/bowl-analysis/analyze \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"bowlId\":\"BOWL_UUID\",\"userMessage\":\"Bowl này có tốt cho sức khỏe không?\"}"
```

### Test Chat
```bash
curl -X POST http://localhost:4458/api/bowl-analysis/chat \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"message\":\"Tôi nên ăn gì để tăng cơ?\"}"
```

---

## Phần 9: Kiểm tra Log

### Xem log trong console để debug:

```
INFO  GeminiServiceImpl : Sending prompt to Gemini API: Bạn là một chuyên gia...
INFO  GeminiServiceImpl : Received response from Gemini API (length: 1234 chars)
INFO  GeminiServiceImpl : Analyzing bowl bowl-uuid-abc123 for user goal: WEIGHT_LOSS
```

---

## Phần 10: Các Kịch Bản Test Khác

### Test với User có Goal khác nhau:

1. **WEIGHT_LOSS (Giảm cân)**
   ```json
   {"userMessage": "Có phù hợp để giảm cân không?"}
   ```

2. **MUSCLE_GAIN (Tăng cơ)**
   ```json
   {"userMessage": "Đủ protein để tăng cơ chưa?"}
   ```

3. **MAINTAIN_HEALTH (Duy trì sức khỏe)**
   ```json
   {"userMessage": "Bowl này có cân bằng dinh dưỡng không?"}
   ```

---

## Checklist Test ✅

- [ ] Ứng dụng đã chạy thành công
- [ ] Đã login và lấy JWT token
- [ ] Test `/api/bowl-analysis/test` - Kết nối Gemini thành công
- [ ] Đã lấy được bowl ID từ database
- [ ] Test phân tích bowl - Câu hỏi chung
- [ ] Test phân tích bowl - Về giảm cân
- [ ] Test phân tích bowl - Về dinh dưỡng
- [ ] Test phân tích bowl - Về thời điểm ăn
- [ ] Test chat đơn giản - Câu hỏi về tăng cơ
- [ ] Test chat đơn giản - Lời khuyên giảm cân
- [ ] Kiểm tra response có markdown formatting
- [ ] Kiểm tra response bằng tiếng Việt

---

## ❌ Troubleshooting

### Lỗi: "Bowl not found"
**Nguyên nhân:** Bowl ID không tồn tại hoặc đã bị xóa
**Giải pháp:** Lấy bowl ID mới từ `/api/orders/getall`

### Lỗi: "401 Unauthorized"
**Nguyên nhân:** JWT token không hợp lệ hoặc hết hạn
**Giải pháp:** Login lại để lấy token mới

### Lỗi: "Failed to generate content from Gemini API"
**Nguyên nhân:** API key sai hoặc hết quota
**Giải pháp:** 
- Kiểm tra API key trong `application.yml`
- Kiểm tra quota tại https://aistudio.google.com/

### Response quá chậm
**Nguyên nhân:** Gemini API processing time
**Giải pháp:** 
- Tăng timeout trong `application.yml`
- Prompt quá dài, rút ngắn lại

---

## 📊 Kết quả mong đợi

✅ **Response thành công** sẽ có:
- Status: 200
- Message: "Bowl analyzed successfully" hoặc "Chat response generated"
- Data: Chứa phân tích chi tiết bằng tiếng Việt với markdown formatting

✅ **AI Response** sẽ bao gồm:
- Đánh giá chung về bowl
- Phân tích dinh dưỡng (calories, protein, etc.)
- Điểm mạnh và điểm cần cải thiện
- Lời khuyên cụ thể
- Trả lời trực tiếp câu hỏi của user

---

**Chúc bạn test thành công! 🚀**

Nếu gặp vấn đề gì, hãy kiểm tra log trong console để debug.

