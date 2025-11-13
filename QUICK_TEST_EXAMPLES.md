# TEST NHANH - Copy & Paste để test

## 🔥 Các request mẫu sẵn sàng để test

### 1️⃣ LOGIN (Lấy JWT Token trước)

```http
POST http://localhost:4458/api/auth/login
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "admin123"
}
```

**→ Copy `accessToken` từ response để dùng cho các bước sau**

---

### 2️⃣ TEST GEMINI CONNECTION

```http
GET http://localhost:4458/api/bowl-analysis/test
Authorization: Bearer PASTE_YOUR_TOKEN_HERE
```

**✅ Nếu thành công sẽ thấy: "Gemini API đang hoạt động tốt!"**

---

### 3️⃣ LẤY DANH SÁCH ORDERS (để lấy bowl ID)

```http
GET http://localhost:4458/api/orders/getall?page=0&size=5
Authorization: Bearer PASTE_YOUR_TOKEN_HERE
```

**→ Copy một `bowl.id` từ response**

---

### 4️⃣ TEST PHÂN TÍCH BOWL - Câu hỏi chung

```http
POST http://localhost:4458/api/bowl-analysis/analyze
Authorization: Bearer PASTE_YOUR_TOKEN_HERE
Content-Type: application/json

{
  "bowlId": "PASTE_BOWL_ID_HERE",
  "userMessage": "Bowl này có tốt cho sức khỏe không?"
}
```

---

### 5️⃣ TEST PHÂN TÍCH BOWL - Giảm cân

```http
POST http://localhost:4458/api/bowl-analysis/analyze
Authorization: Bearer PASTE_YOUR_TOKEN_HERE
Content-Type: application/json

{
  "bowlId": "PASTE_BOWL_ID_HERE",
  "userMessage": "Bowl này có phù hợp với mục tiêu giảm cân của tôi không? Tôi nên thêm hoặc bớt nguyên liệu gì?"
}
```

---

### 6️⃣ TEST PHÂN TÍCH BOWL - Dinh dưỡng chi tiết

```http
POST http://localhost:4458/api/bowl-analysis/analyze
Authorization: Bearer PASTE_YOUR_TOKEN_HERE
Content-Type: application/json

{
  "bowlId": "PASTE_BOWL_ID_HERE",
  "userMessage": "Cho tôi biết thông tin dinh dưỡng chi tiết của bowl này: calories, protein, carbs, fats, vitamin?"
}
```

---

### 7️⃣ TEST PHÂN TÍCH BOWL - Thời điểm ăn

```http
POST http://localhost:4458/api/bowl-analysis/analyze
Authorization: Bearer PASTE_YOUR_TOKEN_HERE
Content-Type: application/json

{
  "bowlId": "PASTE_BOWL_ID_HERE",
  "userMessage": "Bowl này nên ăn vào thời điểm nào trong ngày? Sáng, trưa hay tối? Tại sao?"
}
```

---

### 8️⃣ TEST PHÂN TÍCH BOWL - Tăng cơ

```http
POST http://localhost:4458/api/bowl-analysis/analyze
Authorization: Bearer PASTE_YOUR_TOKEN_HERE
Content-Type: application/json

{
  "bowlId": "PASTE_BOWL_ID_HERE",
  "userMessage": "Bowl này có đủ protein để tăng cơ không? Tôi có nên thêm thịt gà hoặc trứng không?"
}
```

---

### 9️⃣ TEST CHAT - Câu hỏi chung về tăng cơ

```http
POST http://localhost:4458/api/bowl-analysis/chat
Authorization: Bearer PASTE_YOUR_TOKEN_HERE
Content-Type: application/json

{
  "message": "Tôi nên ăn gì để tăng cơ hiệu quả? Cho tôi một thực đơn mẫu."
}
```

---

### 🔟 TEST CHAT - Lời khuyên giảm cân

```http
POST http://localhost:4458/api/bowl-analysis/chat
Authorization: Bearer PASTE_YOUR_TOKEN_HERE
Content-Type: application/json

{
  "message": "Để giảm cân hiệu quả tôi nên ăn những gì và tránh những gì? Cho tôi 5 tips cụ thể."
}
```

---

### 1️⃣1️⃣ TEST CHAT - Nguồn protein

```http
POST http://localhost:4458/api/bowl-analysis/chat
Authorization: Bearer PASTE_YOUR_TOKEN_HERE
Content-Type: application/json

{
  "message": "Những nguồn protein tốt nhất cho người tập gym là gì? Hãy so sánh giữa thịt gà, cá, và đậu."
}
```

---

### 1️⃣2️⃣ TEST CHAT - Thực đơn healthy

```http
POST http://localhost:4458/api/bowl-analysis/chat
Authorization: Bearer PASTE_YOUR_TOKEN_HERE
Content-Type: application/json

{
  "message": "Gợi ý cho tôi một thực đơn healthy cho cả tuần để duy trì sức khỏe."
}
```

---

## 📝 HƯỚNG DẪN SỬ DỤNG

### Với Visual Studio Code + REST Client Extension:

1. Cài extension: **REST Client**
2. Tạo file mới: `test-api.http`
3. Copy các request trên vào file
4. Thay `PASTE_YOUR_TOKEN_HERE` bằng token thực
5. Thay `PASTE_BOWL_ID_HERE` bằng bowl ID thực
6. Click vào **Send Request** phía trên mỗi request

### Với Postman:

1. Tạo new request
2. Copy method và URL
3. Vào tab **Headers**, thêm:
   - `Authorization`: `Bearer YOUR_TOKEN`
   - `Content-Type`: `application/json`
4. Vào tab **Body**, chọn **raw** và **JSON**
5. Paste JSON body
6. Click **Send**

### Với cURL (Terminal):

**Test Gemini Connection:**
```bash
curl -X GET "http://localhost:4458/api/bowl-analysis/test" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Test Analyze Bowl:**
```bash
curl -X POST "http://localhost:4458/api/bowl-analysis/analyze" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"bowlId\":\"YOUR_BOWL_ID\",\"userMessage\":\"Bowl này có tốt không?\"}"
```

**Test Chat:**
```bash
curl -X POST "http://localhost:4458/api/bowl-analysis/chat" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"message\":\"Tôi nên ăn gì?\"}"
```

---

## ✅ KẾT QUẢ MONG ĐỢI

### Response thành công từ Analyze Bowl:

```json
{
  "status": 200,
  "message": "Bowl analyzed successfully",
  "data": {
    "bowlId": "abc-123-xyz",
    "bowlName": "Healthy Salad Bowl",
    "userGoal": "WEIGHT_LOSS",
    "userMessage": "Bowl này có tốt cho sức khỏe không?",
    "aiResponse": "## Đánh giá chung\n\nBowl này rất phù hợp với mục tiêu giảm cân của bạn...\n\n## Phân tích dinh dưỡng\n\n- **Calories**: Khoảng 350-400 kcal\n- **Protein**: 25-30g\n...",
    "analyzedAt": "2025-11-13T12:30:00Z",
    "estimatedCalories": null,
    "estimatedProtein": null
  }
}
```

### Response thành công từ Chat:

```json
{
  "status": 200,
  "message": "Chat response generated",
  "data": "Để tăng cơ hiệu quả, bạn nên:\n\n1. **Tăng lượng protein**: Ăn 1.6-2.2g protein/kg cân nặng...\n\n2. **Ăn đủ calories**: Tăng 300-500 kcal so với TDEE...\n\n..."
}
```

---

## 🎯 TIPS

1. **Test theo thứ tự**: Login → Test Connection → Get Bowl ID → Analyze Bowl
2. **Lưu token**: Lưu JWT token vào một file text để dùng lại
3. **Kiểm tra log**: Xem console để thấy request/response với Gemini API
4. **Thử nhiều câu hỏi**: AI sẽ cho câu trả lời khác nhau tùy vào cách hỏi
5. **Note bowl ID**: Lưu các bowl ID hay dùng để test nhanh

---

**Happy Testing! 🚀**

