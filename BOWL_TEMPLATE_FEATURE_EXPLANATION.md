# Giải Thích Chi Tiết Chức Năng Bowl Template

## 📋 Tổng Quan

Chức năng **Bowl Template** cho phép hệ thống tạo sẵn các mẫu Bowl với định lượng nguyên liệu mặc định. Người dùng có thể chọn template có sẵn để tạo Bowl nhanh chóng thay vì phải chọn từng nguyên liệu một.

## 🏗️ Kiến Trúc Hệ Thống

### 1. Database Schema

```
┌─────────────────────┐
│   bowl_templates    │
├─────────────────────┤
│ id (PK)            │
│ name               │
│ description        │
│ image_url          │
│ estimated_calories │
│ estimated_price    │
│ is_active          │
│ created_at         │
└─────────────────────┘
          │
          │ 1:N
          ▼
┌─────────────────────┐
│   template_steps    │
├─────────────────────┤
│ id (PK)            │
│ template_id (FK)   │───► bowl_templates.id
│ category_id (FK)   │───► categories.id
│ min_items          │
│ max_items          │
│ default_qty        │
│ display_order      │
│ default_ingredients│ (JSON column)
└─────────────────────┘
```

### 2. JSON Structure của `default_ingredients`

Column `default_ingredients` trong table `template_steps` lưu trữ danh sách nguyên liệu mặc định dưới dạng JSON:

```json
[
  {
    "ingredientId": "ing-001",
    "quantity": 100.0,
    "isDefault": true
  },
  {
    "ingredientId": "ing-002",
    "quantity": 50.0,
    "isDefault": true
  },
  {
    "ingredientId": "ing-003",
    "quantity": 30.0,
    "isDefault": false
  }
]
```

**Ý nghĩa các trường:**
- `ingredientId`: ID của ingredient (KHÔNG phải foreign key, chỉ là string ID)
- `quantity`: Số lượng mặc định (gram/ml)
- `isDefault`: `true` = tự động chọn khi tạo bowl, `false` = tùy chọn

## 🔄 Luồng Hoạt Động

### Workflow 1: Tạo Bowl Template

```
Admin/Manager
    │
    │ POST /api/bowl_templates/create
    ▼
┌──────────────────────────────┐
│ BowlTemplateController       │
│   - create(request)          │
└──────────────────────────────┘
    │
    │ mapper.toEntity()
    ▼
┌──────────────────────────────┐
│ BowlTemplateServiceImpl      │
│   - create(entity)           │
│   - Lưu template & steps     │
└──────────────────────────────┘
    │
    ▼
┌──────────────────────────────┐
│ TemplateStepServiceImpl      │
│   - Lưu steps với            │
│     default_ingredients      │
└──────────────────────────────┘
    │
    ▼
Database
```

**Request Example:**
```json
{
  "name": "Healthy Protein Bowl",
  "description": "Bowl giàu protein cho gym",
  "imageUrl": "https://...",
  "estimatedCalories": 450,
  "estimatedPrice": 65000,
  "steps": [
    {
      "categoryId": "cat-protein",
      "minItems": 1,
      "maxItems": 2,
      "defaultQty": 150.0,
      "displayOrder": 1,
      "defaultIngredients": [
        {
          "ingredientId": "chicken-breast",
          "quantity": 150.0,
          "isDefault": true
        },
        {
          "ingredientId": "tofu",
          "quantity": 100.0,
          "isDefault": false
        }
      ]
    },
    {
      "categoryId": "cat-carbs",
      "minItems": 1,
      "maxItems": 1,
      "defaultQty": 100.0,
      "displayOrder": 2,
      "defaultIngredients": [
        {
          "ingredientId": "brown-rice",
          "quantity": 100.0,
          "isDefault": true
        }
      ]
    }
  ]
}
```

### Workflow 2: Hiển Thị Template cho User

```
User/Customer
    │
    │ GET /api/bowl_templates/getall
    ▼
┌──────────────────────────────┐
│ BowlTemplateController       │
│   - getAll()                 │
└──────────────────────────────┘
    │
    ▼
┌──────────────────────────────┐
│ BowlTemplateServiceImpl      │
│   - findAllWithSteps()       │
└──────────────────────────────┘
    │
    ▼
┌──────────────────────────────┐
│ TemplateStepEnrichmentService│
│   - Enrich ingredient info   │
│   - Thêm tên, giá, đơn vị    │
└──────────────────────────────┘
    │
    ▼
Response với đầy đủ thông tin
```

**Response Example:**
```json
{
  "code": 200,
  "message": "Bowl templates retrieved successfully",
  "data": {
    "content": [
      {
        "id": "tmpl-001",
        "name": "Healthy Protein Bowl",
        "description": "Bowl giàu protein cho gym",
        "imageUrl": "https://...",
        "estimatedCalories": 450,
        "estimatedPrice": 65000,
        "steps": [
          {
            "id": "step-001",
            "categoryId": "cat-protein",
            "category": {
              "id": "cat-protein",
              "name": "Protein",
              "kind": "PROTEIN"
            },
            "minItems": 1,
            "maxItems": 2,
            "defaultQty": 150.0,
            "displayOrder": 1,
            "defaultIngredients": [
              {
                "ingredientId": "chicken-breast",
                "ingredientName": "Ức gà",
                "quantity": 150.0,
                "isDefault": true,
                "unitPrice": 200.0,
                "unit": "gram"
              },
              {
                "ingredientId": "tofu",
                "ingredientName": "Đậu hũ",
                "quantity": 100.0,
                "isDefault": false,
                "unitPrice": 50.0,
                "unit": "gram"
              }
            ]
          }
        ]
      }
    ],
    "page": 0,
    "size": 5,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### Workflow 3: Tạo Bowl từ Template

```
User/Customer
    │
    │ POST /api/bowls/create-from-template
    │ {
    │   "orderId": "ord-123",
    │   "templateId": "tmpl-001",
    │   "customName": "My Custom Bowl",
    │   "customQuantities": {
    │     "chicken-breast": 200.0  // Override
    │   }
    │ }
    ▼
┌──────────────────────────────┐
│ BowlController               │
│   - createFromTemplate()     │
└──────────────────────────────┘
    │
    ▼
┌──────────────────────────────┐
│ BowlServiceImpl              │
│   - createFromTemplate()     │
└──────────────────────────────┘
    │
    │ Bước 1: Lấy template
    ▼
┌──────────────────────────────┐
│ BowlTemplateRepository       │
│   - findByIdWithSteps()      │
└──────────────────────────────┘
    │
    │ Bước 2: Tạo Bowl entity
    ▼
┌──────────────────────────────┐
│ Bowl                         │
│   - name = template.name     │
│   - orderId = orderId        │
│   - templateId = templateId  │
└──────────────────────────────┘
    │
    │ Bước 3: Tạo BowlItems
    ▼
┌──────────────────────────────────────┐
│ Với mỗi step trong template:        │
│                                      │
│ 1. Lấy defaultIngredients (JSON)    │
│ 2. Filter những item có isDefault=true│
│ 3. Tạo BowlItem cho mỗi ingredient  │
│ 4. Quantity = customQuantities      │
│    hoặc default quantity            │
└──────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────┐
│ BowlItemServiceImpl          │
│   - create() cho từng item   │
└──────────────────────────────┘
    │
    ▼
Database
    │
    ▼
Response: Bowl với danh sách items
```

## 🔑 Key Components

### 1. Models

#### TemplateStep.java
```java
@Entity
@Table(name = "template_steps")
public class TemplateStep {
    @Id
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private BowlTemplate template;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;
    
    private Integer minItems;
    private Integer maxItems;
    private Double defaultQty;
    private Integer displayOrder;
    
    // JSON column - KHÔNG phải foreign key
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "default_ingredients", columnDefinition = "json")
    private List<DefaultIngredientItem> defaultIngredients;
    
    @Data
    public static class DefaultIngredientItem {
        private String ingredientId;  // Chỉ là String, không phải FK
        private Double quantity;
        private Boolean isDefault;
    }
}
```

**⚠️ Quan trọng:** `ingredientId` trong JSON **KHÔNG phải** foreign key constraint trong database. Đây chỉ là string ID được lưu trong JSON. Lý do:

1. **Tính linh hoạt**: JSON có thể thay đổi dễ dàng mà không cần migration
2. **Performance**: Không cần join nhiều bảng khi query
3. **Simplicity**: Dễ dàng thêm/xóa ingredients trong template

### 2. Services

#### TemplateStepEnrichmentService
Service này có nhiệm vụ "làm giàu" thông tin cho `defaultIngredients`:

```java
public void enrichDefaultIngredients(
    TemplateStepResponse response, 
    TemplateStep entity
) {
    // Lấy danh sách ingredient IDs từ JSON
    List<String> ingredientIds = entity.getDefaultIngredients()
        .stream()
        .map(item -> item.getIngredientId())
        .toList();
    
    // Query database để lấy thông tin đầy đủ
    Map<String, Ingredient> ingredientMap = 
        ingredientRepository.findAllById(ingredientIds)
            .stream()
            .collect(Collectors.toMap(
                Ingredient::getId, 
                Function.identity()
            ));
    
    // Enrich response với thông tin đầy đủ
    List<DefaultIngredientItemDto> enrichedItems = 
        entity.getDefaultIngredients().stream()
            .map(item -> {
                Ingredient ing = ingredientMap.get(item.getIngredientId());
                return new DefaultIngredientItemDto(
                    item.getIngredientId(),
                    ing.getName(),           // Tên để hiển thị
                    item.getQuantity(),
                    item.getIsDefault(),
                    ing.getUnitPrice(),      // Giá để tính toán
                    ing.getUnit()            // Đơn vị
                );
            })
            .toList();
    
    response.setDefaultIngredients(enrichedItems);
}
```

#### BowlServiceImpl.createFromTemplate()
```java
public Bowl createFromTemplate(
    String orderId, 
    String templateId, 
    Map<String, Double> customQuantities
) {
    // 1. Lấy template với steps
    BowlTemplate template = templateRepository
        .findByIdWithSteps(templateId)
        .orElseThrow(() -> new RuntimeException("Template not found"));
    
    // 2. Tạo Bowl entity
    Bowl bowl = new Bowl();
    bowl.setId(UUID.randomUUID().toString());
    bowl.setName(template.getName());
    bowl.setOrderId(orderId);
    bowl.setTemplateId(templateId);
    bowl.setItems(new ArrayList<>());
    
    Bowl savedBowl = bowlRepository.save(bowl);
    
    // 3. Tạo BowlItems từ defaultIngredients
    for (TemplateStep step : template.getSteps()) {
        // Lấy ingredients có isDefault = true
        List<DefaultIngredientItem> defaultItems = 
            step.getDefaultIngredients().stream()
                .filter(item -> item.getIsDefault() == true)
                .toList();
        
        for (DefaultIngredientItem item : defaultItems) {
            // Query ingredient để lấy thông tin
            Ingredient ingredient = ingredientRepository
                .findById(item.getIngredientId())
                .orElseThrow(() -> new RuntimeException(
                    "Ingredient not found: " + item.getIngredientId()
                ));
            
            // Tạo BowlItem
            BowlItem bowlItem = new BowlItem();
            bowlItem.setId(UUID.randomUUID().toString());
            bowlItem.setBowl(savedBowl);
            bowlItem.setIngredient(ingredient);
            
            // Quantity: custom hoặc default
            Double qty = customQuantities != null 
                ? customQuantities.getOrDefault(
                    item.getIngredientId(), 
                    item.getQuantity()
                  )
                : item.getQuantity();
            
            bowlItem.setQty(qty);
            bowlItem.setUnitPrice(ingredient.getUnitPrice());
            
            savedBowl.getItems().add(bowlItemRepository.save(bowlItem));
        }
    }
    
    return savedBowl;
}
```

## 📊 Data Flow Diagram

### Tạo Bowl từ Template

```
┌─────────────┐
│   Request   │
│ templateId  │
│ orderId     │
│ customQty   │
└──────┬──────┘
       │
       ▼
┌──────────────────────────────────┐
│  1. Query Template with Steps    │
│     SELECT * FROM bowl_templates │
│     JOIN template_steps          │
│     WHERE id = templateId        │
└──────────────┬───────────────────┘
               │
               ▼
        ┌──────────────┐
        │  Template    │
        │    Steps     │
        │   (JSON)     │
        └──────┬───────┘
               │
               ▼
┌──────────────────────────────────┐
│  2. Parse JSON default_ingredients│
│     Extract ingredientIds        │
└──────────────┬───────────────────┘
               │
               ▼
┌──────────────────────────────────┐
│  3. Query Ingredients            │
│     SELECT * FROM ingredients    │
│     WHERE id IN (...)            │
└──────────────┬───────────────────┘
               │
               ▼
┌──────────────────────────────────┐
│  4. Create Bowl                  │
│     INSERT INTO bowls            │
└──────────────┬───────────────────┘
               │
               ▼
┌──────────────────────────────────┐
│  5. Create BowlItems             │
│     For each default ingredient: │
│     INSERT INTO bowl_items       │
└──────────────┬───────────────────┘
               │
               ▼
        ┌──────────────┐
        │   Response   │
        │  Bowl + Items│
        └──────────────┘
```

## 🎯 Use Cases

### Use Case 1: Admin tạo template mới

**Actors:** Admin/Manager

**Steps:**
1. Admin đăng nhập hệ thống
2. Vào trang quản lý Bowl Templates
3. Click "Create New Template"
4. Nhập thông tin template (name, description, image)
5. Thêm các steps:
   - Chọn category (Protein, Carbs, Vegetables, etc.)
   - Set min/max items cho step
   - Thêm default ingredients với quantity
   - Đánh dấu ingredients nào là default (isDefault=true)
6. Save template
7. Hệ thống lưu vào database với JSON structure

### Use Case 2: Customer chọn template để order

**Actors:** Customer

**Steps:**
1. Customer browse menu
2. Xem danh sách Bowl Templates
3. Chọn template "Healthy Protein Bowl"
4. Xem preview với default ingredients
5. (Optional) Customize:
   - Thay đổi quantity của ingredients
   - Thêm tên custom cho bowl
   - Thêm instruction đặc biệt
6. Add to cart (tạo bowl từ template)
7. Hệ thống tạo Bowl với BowlItems theo default hoặc custom

### Use Case 3: System tính giá tự động

**Flow:**
```
Template (estimatedPrice: 65000)
    │
    └─► Step 1: Protein
        │   └─► Chicken (150g × 200đ/g = 30,000đ) ✓ default
        │   └─► Tofu (100g × 50đ/g = 5,000đ) ✗ not default
        │
    └─► Step 2: Carbs  
        └─► Brown Rice (100g × 100đ/g = 10,000đ) ✓ default

Actual Price = 30,000 + 10,000 = 40,000đ
(estimatedPrice chỉ là tham khảo, giá thực tính từ ingredients)
```

## ⚡ Performance Optimization

### 1. Lazy Loading
```java
@ManyToOne(fetch = FetchType.LAZY)
private BowlTemplate template;
```
Template chỉ được load khi cần thiết

### 2. Batch Query cho Ingredients
```java
// Thay vì N queries
for (id : ingredientIds) {
    ingredientRepo.findById(id);  // ❌ N queries
}

// Dùng 1 query
ingredientRepo.findAllById(ingredientIds);  // ✅ 1 query
```

### 3. Caching
```java
@Cacheable("templates")
public Optional<BowlTemplate> findByIdWithSteps(String id) {
    // Cache template để tránh query lại
}
```

## 🔒 Security & Validation

### 1. Validation tại Controller
```java
@Valid @RequestBody BowlTemplateRequest req
```

### 2. Business Logic Validation
```java
// Kiểm tra ingredient có tồn tại không
if (!ingredientRepo.existsById(ingredientId)) {
    throw new NotFoundException("Ingredient not found");
}

// Kiểm tra quantity hợp lệ
if (quantity <= 0) {
    throw new ValidationException("Quantity must be positive");
}
```

### 3. Authorization
```java
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public ResponseEntity<ApiResponse<BowlTemplateResponse>> create(...)
```

## 🐛 Error Handling

### Các trường hợp lỗi thường gặp:

1. **Template not found**
```json
{
  "code": 404,
  "error": "NOT_FOUND",
  "message": "Template not found: tmpl-xxx"
}
```

2. **Ingredient in template không tồn tại**
```json
{
  "code": 400,
  "error": "INVALID_INGREDIENT",
  "message": "Ingredient not found: ing-xxx in template step"
}
```

3. **Order không tồn tại**
```json
{
  "code": 404,
  "error": "ORDER_NOT_FOUND",
  "message": "Order not found: ord-xxx"
}
```

## 📝 Best Practices

### 1. Tại sao lưu ingredientId thay vì foreign key?

**Ưu điểm:**
- ✅ Flexible: Dễ thêm/sửa/xóa ingredients trong template
- ✅ Performance: Không cần join khi query template
- ✅ Simple migration: Thay đổi JSON không cần alter table

**Nhược điểm:**
- ❌ Data integrity: Không có constraint từ database
- ❌ Orphan data: Nếu xóa ingredient, template vẫn giữ ID cũ

**Giải pháp:**
```java
// Validation khi tạo/update template
public void validateIngredients(List<String> ingredientIds) {
    List<String> existing = ingredientRepo.findAllById(ingredientIds)
        .stream()
        .map(Ingredient::getId)
        .toList();
    
    List<String> notFound = ingredientIds.stream()
        .filter(id -> !existing.contains(id))
        .toList();
    
    if (!notFound.isEmpty()) {
        throw new ValidationException(
            "Ingredients not found: " + String.join(", ", notFound)
        );
    }
}
```

### 2. Khi nào nên dùng Template?

**Nên dùng khi:**
- Có các món ăn cố định, phổ biến
- Muốn đơn giản hóa order process
- Có combo/set menu

**Không nên dùng khi:**
- 100% customization
- Menu thay đổi liên tục
- Mỗi order đều unique

### 3. Cache Strategy

```java
// Cache template list (invalidate khi có update)
@Cacheable(value = "templates", key = "'all'")
public List<BowlTemplate> findAllWithSteps() { ... }

// Clear cache khi update
@CacheEvict(value = "templates", allEntries = true)
public BowlTemplate update(String id, BowlTemplate entity) { ... }
```

## 🚀 Future Enhancements

### 1. Template Versioning
Lưu các version khác nhau của template để tracking changes

### 2. A/B Testing
Test nhiều template variants để xem cái nào popular hơn

### 3. Personalized Templates
Tạo template dựa trên order history của customer

### 4. Seasonal Templates
Auto-enable/disable templates theo mùa hoặc thời gian

## 📞 API Endpoints Summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/bowl_templates/getall` | Lấy tất cả templates |
| GET | `/api/bowl_templates/getbyid/{id}` | Lấy template theo ID |
| POST | `/api/bowl_templates/create` | Tạo template mới |
| PUT | `/api/bowl_templates/update/{id}` | Cập nhật template |
| DELETE | `/api/bowl_templates/delete/{id}` | Xóa template (soft delete) |
| POST | `/api/bowls/create-from-template` | Tạo bowl từ template |

---

**Tài liệu được tạo:** 11/11/2025  
**Version:** 1.0  
**Author:** Development Team

