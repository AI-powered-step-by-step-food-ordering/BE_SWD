# ✅ TRIỂN KHAI BOWL TEMPLATE VỚI DEFAULT INGREDIENTS

## 🎯 MỤC TIÊU ĐÃ ĐẠT ĐƯỢC

Cho phép người dùng **chọn BowlTemplate có sẵn định lượng ingredient**, tự động tạo Bowl với BowlItems theo template mà **KHÔNG CẦN TẠO TABLE MỚI**.

---

## 📊 GIẢI PHÁP: SỬ DỤNG JSON FIELD

Thay vì tạo table `TemplateIngredient` mới, sử dụng **JSON field** trong bảng `template_steps` để lưu danh sách ingredient mặc định.

### **Ưu điểm:**
- ✅ Không cần tạo table mới
- ✅ Đơn giản hơn, ít join hơn
- ✅ Linh hoạt - dễ thêm/sửa default ingredients
- ✅ Performance tốt hơn (ít query hơn)
- ✅ Phù hợp với use case: template được định nghĩa sẵn, ít thay đổi

---

## 🗂️ CÁC FILE ĐÃ TẠO MỚI

### 1. **TemplateStepEnrichmentService.java**
- Service để enrich defaultIngredients với thông tin ingredient đầy đủ
- Load ingredients từ database và gắn vào response
- Path: `src/main/java/com/officefood/healthy_food_api/service/TemplateStepEnrichmentService.java`

### 2. **CreateBowlFromTemplateRequest.java**
- DTO cho request tạo Bowl từ template
- Hỗ trợ custom name, instruction, và custom quantities
- Path: `src/main/java/com/officefood/healthy_food_api/dto/request/CreateBowlFromTemplateRequest.java`

---

## 📝 CÁC FILE ĐÃ CHỈNH SỬA

### 1. **Model: TemplateStep.java**
**Thêm:**
```java
@JdbcTypeCode(SqlTypes.JSON)
@Column(name="default_ingredients", columnDefinition = "JSON")
private List<DefaultIngredientItem> defaultIngredients = new ArrayList<>();

@Embeddable
@Data
public static class DefaultIngredientItem {
    private String ingredientId;
    private Double quantity;
    private Boolean isDefault; // True = tự động thêm, False = chỉ gợi ý
}
```

**Cấu trúc JSON:**
```json
[
  {
    "ingredientId": "ingredient-uuid-1",
    "quantity": 200.0,
    "isDefault": true
  },
  {
    "ingredientId": "ingredient-uuid-2",
    "quantity": 100.0,
    "isDefault": false
  }
]
```

---

### 2. **DTO Request: TemplateStepRequest.java**
**Thêm:**
```java
private List<DefaultIngredientItemRequest> defaultIngredients;

public static class DefaultIngredientItemRequest {
    @NotNull private String ingredientId;
    @NotNull private Double quantity;
    private Boolean isDefault; // Mặc định là true
}
```

---

### 3. **DTO Response: TemplateStepResponse.java**
**Thêm:**
```java
private List<DefaultIngredientItemDto> defaultIngredients;

public static class DefaultIngredientItemDto {
    private String ingredientId;
    private String ingredientName;    // Tên ingredient
    private Double quantity;
    private Boolean isDefault;
    private Double unitPrice;         // Giá để tính toán
    private String unit;              // Đơn vị
}
```

**Response mẫu:**
```json
{
  "id": "step-1",
  "category": { "name": "Base" },
  "minItems": 1,
  "maxItems": 2,
  "defaultIngredients": [
    {
      "ingredientId": "ing-1",
      "ingredientName": "White Rice",
      "quantity": 200.0,
      "isDefault": true,
      "unitPrice": 5000.0,
      "unit": "gram"
    }
  ]
}
```

---

### 4. **Mapper: TemplateStepMapper.java**
**Thêm:**
```java
// Map từ request DTO sang entity
default List<TemplateStep.DefaultIngredientItem> mapRequestToDefaultIngredients(
        List<TemplateStepRequest.DefaultIngredientItemRequest> requests) {
    // Convert request -> entity default ingredients
}
```

---

### 5. **Service: BowlService.java**
**Thêm methods:**
```java
Bowl createFromTemplate(String orderId, String templateId);
Bowl createFromTemplate(String orderId, String templateId, Map<String, Double> customQuantities);
```

---

### 6. **Service Impl: BowlServiceImpl.java**
**Thêm dependencies:**
```java
private final BowlTemplateRepository templateRepository;
private final OrderRepository orderRepository;
private final IngredientRepository ingredientRepository;
```

**Logic createFromTemplate:**
1. Load order
2. Load template với steps
3. Tạo Bowl mới
4. Thu thập tất cả ingredientIds từ defaultIngredients (isDefault=true)
5. Load tất cả ingredients trong 1 query
6. Tạo BowlItems với quantity và tính giá
7. Tính tổng linePrice
8. Save và return

**Công thức tính giá:**
```java
itemPrice = (quantity / standardQuantity) * unitPrice
```

---

### 7. **Controller: BowlTemplateController.java**
**Inject service:**
```java
private final TemplateStepEnrichmentService enrichmentService;
```

**Override toResponse:**
```java
protected BowlTemplateResponse toResponse(BowlTemplate entity) {
    BowlTemplateResponse response = mapper.toResponse(entity);
    // Enrich defaultIngredients với thông tin ingredient đầy đủ
    entity.getSteps().forEach(step -> {
        // Find matching step response và enrich
        enrichmentService.enrichDefaultIngredients(stepRes, step);
    });
    return response;
}
```

---

### 8. **Controller: BowlController.java**
**Endpoint mới:**
```java
POST /api/bowls/create-from-template
```

**Request body:**
```json
{
  "orderId": "order-uuid",
  "templateId": "template-uuid",
  "customName": "My Custom Bowl",           // Optional
  "instruction": "Less spicy please",        // Optional
  "customQuantities": {                      // Optional
    "ingredient-uuid-1": 150.0,
    "ingredient-uuid-2": 80.0
  }
}
```

**Response:**
```json
{
  "status": 201,
  "message": "Bowl created from template successfully with 5 items",
  "data": {
    "id": "bowl-uuid",
    "name": "My Custom Bowl",
    "linePrice": 75000.0,
    "items": [...]
  }
}
```

---

## 🔄 LUỒNG SỬ DỤNG

### **Admin - Thiết lập Template:**

1. **Tạo BowlTemplate** (đã có sẵn)
   ```
   POST /api/bowl_templates/create
   ```

2. **Tạo TemplateStep với default ingredients** (CẬP NHẬT)
   ```
   POST /api/template_steps/create
   Body: {
     "templateId": "template-uuid",
     "categoryId": "category-uuid",
     "minItems": 1,
     "maxItems": 2,
     "defaultIngredients": [
       {
         "ingredientId": "rice-uuid",
         "quantity": 200.0,
         "isDefault": true
       },
       {
         "ingredientId": "quinoa-uuid",
         "quantity": 200.0,
         "isDefault": false
       }
     ]
   }
   ```

3. **Xem template với ingredients**
   ```
   GET /api/bowl_templates/getbyid/{id}
   Response: Template với steps và defaultIngredients đầy đủ
   ```

---

### **User - Đặt món:**

1. **Xem danh sách templates**
   ```
   GET /api/bowl_templates/getall
   Response: Danh sách templates với ingredients gợi ý
   ```

2. **Chọn template và tạo bowl tự động** (MỚI)
   ```
   POST /api/bowls/create-from-template
   Body: {
     "orderId": "order-uuid",
     "templateId": "template-uuid"
   }
   ```
   → Hệ thống tự động:
   - Tạo Bowl
   - Thêm tất cả ingredients có `isDefault: true`
   - Sử dụng `quantity` mặc định từ template
   - Tính giá tự động
   - Return Bowl hoàn chỉnh

3. **Hoặc tùy chỉnh quantities** (MỚI)
   ```
   POST /api/bowls/create-from-template
   Body: {
     "orderId": "order-uuid",
     "templateId": "template-uuid",
     "customQuantities": {
       "rice-uuid": 150.0,      // Giảm từ 200g → 150g
       "salmon-uuid": 120.0     // Tăng từ 100g → 120g
     }
   }
   ```

---

## 🗄️ DATABASE MIGRATION

**Thêm column vào bảng `template_steps`:**

```sql
ALTER TABLE template_steps 
ADD COLUMN default_ingredients JSON;
```

**Hoặc nếu tạo mới:**
```sql
CREATE TABLE template_steps (
    id VARCHAR(36) PRIMARY KEY,
    template_id VARCHAR(36) NOT NULL,
    category_id VARCHAR(36) NOT NULL,
    min_items INT,
    max_items INT,
    default_qty DOUBLE,
    display_order INT,
    default_ingredients JSON,           -- MỚI
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (template_id) REFERENCES bowls_template(id),
    FOREIGN KEY (category_id) REFERENCES categories(id)
);
```

---

## 📊 SO SÁNH GIẢI PHÁP

| Tiêu chí | Table mới (TemplateIngredient) | JSON field (Đã chọn) |
|----------|-------------------------------|---------------------|
| **Số table** | +1 table | 0 table mới |
| **Foreign Key** | Có FK constraint đến ingredients | **KHÔNG có FK**, chỉ lưu string ID |
| **Số JOIN** | +1-2 JOINs | 0 JOIN thêm |
| **Complexity** | Cao hơn | Thấp hơn |
| **Performance** | Chậm hơn (nhiều JOIN) | Nhanh hơn |
| **Flexibility** | Ít linh hoạt | Rất linh hoạt |
| **Data integrity** | Database enforced (FK) | **Application enforced (code validation)** |
| **Khi ingredient bị xóa** | FK constraint error | **Gracefully skip** |
| **Use case** | Khi cần query theo ingredient | Khi template định nghĩa sẵn |

### **⚠️ LƯU Ý QUAN TRỌNG:**

**Approach hiện tại (JSON field):**
```sql
-- default_ingredients column chỉ lưu JSON text
-- ingredientId là STRING trong JSON, KHÔNG phải Foreign Key
-- Ví dụ:
default_ingredients: '[{"ingredientId":"uuid-123","quantity":200,"isDefault":true}]'
```

**Validation:**
- ✅ Database: KHÔNG validate ingredientId có tồn tại hay không
- ✅ Application: Code Java sẽ validate khi runtime
- ✅ Nếu ingredient không tồn tại → Skip và log warning, không crash

**Code validation example:**
```java
Ingredient ingredient = ingredientRepository.findById(ingredientId);
if (ingredient == null) {
    log.warn("Ingredient {} not found, skipping", ingredientId);
    continue; // Không crash, chỉ skip
}
```

---

## ✅ TESTING

### **Test Case 1: Tạo template với default ingredients**
```bash
POST /api/template_steps/create
{
  "templateId": "...",
  "categoryId": "...",
  "defaultIngredients": [
    {"ingredientId": "rice-1", "quantity": 200, "isDefault": true}
  ]
}
```

### **Test Case 2: Lấy template - xem ingredients**
```bash
GET /api/bowl_templates/getbyid/{id}
# Verify: defaultIngredients có đầy đủ thông tin (name, price, unit)
```

### **Test Case 3: Tạo bowl từ template**
```bash
POST /api/bowls/create-from-template
{
  "orderId": "...",
  "templateId": "..."
}
# Verify: Bowl tạo với đúng số BowlItems, giá tính đúng
```

### **Test Case 4: Tạo bowl với custom quantities**
```bash
POST /api/bowls/create-from-template
{
  "orderId": "...",
  "templateId": "...",
  "customQuantities": {"rice-1": 150}
}
# Verify: Quantity override đúng
```

---

## 🎉 KẾT LUẬN

Đã triển khai **THÀNH CÔNG** tính năng Bowl Template với default ingredients:

✅ **KHÔNG CẦN** tạo table mới  
✅ Sử dụng JSON field - đơn giản và hiệu quả  
✅ Admin có thể thiết lập ingredient mặc định cho template  
✅ User có thể chọn template và tự động tạo bowl  
✅ Hỗ trợ customize quantity nếu muốn  
✅ Tính giá tự động dựa trên quantity  
✅ Code sạch, dễ maintain  

**Performance:** Tốt hơn so với table mới (ít JOIN hơn)  
**Flexibility:** Cao - dễ thêm/sửa ingredients  
**Maintainability:** Tốt - ít code hơn, logic đơn giản hơn  

---

## 📞 SUPPORT

Nếu cần thêm tính năng:
- Validation minItems/maxItems khi tạo bowl
- Suggestions cho ingredients (isDefault=false)
- Pricing calculation với discount
- Inventory check trước khi tạo bowl

Hãy cho biết để tiếp tục phát triển! 🚀

