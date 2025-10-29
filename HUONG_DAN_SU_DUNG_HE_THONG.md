# 📖 HƯỚNG DẪN SỬ DỤNG HỆ THỐNG HEALTHY FOOD API

## 🎯 TỔNG QUAN
Hệ thống cho phép khách hàng tự tạo bát món ăn lành mạnh theo sở thích, với các bước được định nghĩa sẵn bởi Admin.

---

## 👨‍💼 PHẦN 1: LUỒNG ADMIN - THIẾT LẬP HỆ THỐNG

### 📋 THỨ TỰ THỰC HIỆN (BẮT BUỘC)

```
1. Tạo Categories (Danh mục nguyên liệu)
   ↓
2. Tạo Ingredients (Nguyên liệu cụ thể)
   ↓
3. Tạo BowlTemplate (Mẫu bowl)
   ↓
4. Tạo TemplateSteps (Các bước cho template)
   ↓
5. (Tùy chọn) Tạo IngredientRestrictions (Ràng buộc nguyên liệu)
```

---

### BƯỚC 1: TẠO CATEGORIES (Danh mục nguyên liệu)

**Endpoint:** `POST /api/categories/create`

**Mục đích:** Tạo các danh mục để phân loại nguyên liệu

**Các loại Category (IngredientKind):**
- `CARB` - Tinh bột (cơm, mì, khoai...)
- `PROTEIN` - Đạm (thịt, cá, đậu phụ...)
- `VEGGIE` - Rau củ
- `SAUCE` - Nước sốt
- `TOPPING` - Topping
- `OTHER` - Khác

**Ví dụ tạo Categories:**

```json
// Category 1: Base (Chọn cơm/mì)
POST /api/categories/create
{
  "name": "Base - Chọn nền",
  "kind": "CARB",
  "displayOrder": 1,
  "imageUrl": "https://example.com/images/base.jpg"
}

// Category 2: Protein chính
POST /api/categories/create
{
  "name": "Protein - Chọn món chính",
  "kind": "PROTEIN",
  "displayOrder": 2,
  "imageUrl": "https://example.com/images/protein.jpg"
}

// Category 3: Rau củ
POST /api/categories/create
{
  "name": "Rau củ tươi",
  "kind": "VEGGIE",
  "displayOrder": 3,
  "imageUrl": "https://example.com/images/veggies.jpg"
}

// Category 4: Nước sốt
POST /api/categories/create
{
  "name": "Nước sốt - Thêm hương vị",
  "kind": "SAUCE",
  "displayOrder": 4,
  "imageUrl": "https://example.com/images/sauce.jpg"
}

// Category 5: Topping
POST /api/categories/create
{
  "name": "Topping - Hoàn thiện",
  "kind": "TOPPING",
  "displayOrder": 5,
  "imageUrl": "https://example.com/images/topping.jpg"
}
```

**Response mẫu:**
```json
{
  "code": 201,
  "message": "Category created successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "name": "Base - Chọn nền",
    "kind": "CARB",
    "displayOrder": 1,
    "imageUrl": "https://example.com/images/base.jpg"
  }
}
```

**⚠️ LƯU Ý:** Lưu lại các `id` của Categories vừa tạo để dùng cho bước tiếp theo!

---

### BƯỚC 2: TẠO INGREDIENTS (Nguyên liệu)

**Endpoint:** `POST /api/ingredients/create`

**Mục đích:** Tạo các nguyên liệu cụ thể cho từng Category

**Ví dụ tạo Ingredients:**

```json
// === INGREDIENTS CHO CATEGORY "BASE" ===

POST /api/ingredients/create
{
  "name": "Cơm gạo lứt",
  "categoryId": "550e8400-e29b-41d4-a716-446655440001",  // ID của category Base
  "unit": "100g",
  "unitPrice": 10000,
  "imageUrl": "https://example.com/images/brown-rice.jpg"
}

POST /api/ingredients/create
{
  "name": "Cơm trắng",
  "categoryId": "550e8400-e29b-41d4-a716-446655440001",
  "unit": "100g",
  "unitPrice": 8000,
  "imageUrl": "https://example.com/images/white-rice.jpg"
}

POST /api/ingredients/create
{
  "name": "Quinoa",
  "categoryId": "550e8400-e29b-41d4-a716-446655440001",
  "unit": "100g",
  "unitPrice": 15000,
  "imageUrl": "https://example.com/images/quinoa.jpg"
}

POST /api/ingredients/create
{
  "name": "Mì soba",
  "categoryId": "550e8400-e29b-41d4-a716-446655440001",
  "unit": "100g",
  "unitPrice": 12000,
  "imageUrl": "https://example.com/images/soba.jpg"
}

// === INGREDIENTS CHO CATEGORY "PROTEIN" ===

POST /api/ingredients/create
{
  "name": "Ức gà nướng",
  "categoryId": "550e8400-e29b-41d4-a716-446655440002",  // ID của category Protein
  "unit": "100g",
  "unitPrice": 25000,
  "imageUrl": "https://example.com/images/grilled-chicken.jpg"
}

POST /api/ingredients/create
{
  "name": "Cá hồi nướng",
  "categoryId": "550e8400-e29b-41d4-a716-446655440002",
  "unit": "100g",
  "unitPrice": 45000,
  "imageUrl": "https://example.com/images/salmon.jpg"
}

POST /api/ingredients/create
{
  "name": "Đậu phụ chiên giòn",
  "categoryId": "550e8400-e29b-41d4-a716-446655440002",
  "unit": "100g",
  "unitPrice": 15000,
  "imageUrl": "https://example.com/images/tofu.jpg"
}

POST /api/ingredients/create
{
  "name": "Thịt bò Úc",
  "categoryId": "550e8400-e29b-41d4-a716-446655440002",
  "unit": "100g",
  "unitPrice": 35000,
  "imageUrl": "https://example.com/images/beef.jpg"
}

// === INGREDIENTS CHO CATEGORY "VEGGIE" ===

POST /api/ingredients/create
{
  "name": "Xà lách",
  "categoryId": "550e8400-e29b-41d4-a716-446655440003",  // ID của category Veggie
  "unit": "50g",
  "unitPrice": 5000,
  "imageUrl": "https://example.com/images/lettuce.jpg"
}

POST /api/ingredients/create
{
  "name": "Cà chua cherry",
  "categoryId": "550e8400-e29b-41d4-a716-446655440003",
  "unit": "50g",
  "unitPrice": 6000,
  "imageUrl": "https://example.com/images/tomato.jpg"
}

POST /api/ingredients/create
{
  "name": "Dưa chuột",
  "categoryId": "550e8400-e29b-41d4-a716-446655440003",
  "unit": "50g",
  "unitPrice": 4000,
  "imageUrl": "https://example.com/images/cucumber.jpg"
}

POST /api/ingredients/create
{
  "name": "Cà rốt baby",
  "categoryId": "550e8400-e29b-41d4-a716-446655440003",
  "unit": "50g",
  "unitPrice": 5000,
  "imageUrl": "https://example.com/images/carrot.jpg"
}

POST /api/ingredients/create
{
  "name": "Bắp cải tím",
  "categoryId": "550e8400-e29b-41d4-a716-446655440003",
  "unit": "50g",
  "unitPrice": 4000,
  "imageUrl": "https://example.com/images/cabbage.jpg"
}

// === INGREDIENTS CHO CATEGORY "SAUCE" ===

POST /api/ingredients/create
{
  "name": "Nước sốt Teriyaki",
  "categoryId": "550e8400-e29b-41d4-a716-446655440004",  // ID của category Sauce
  "unit": "30ml",
  "unitPrice": 8000,
  "imageUrl": "https://example.com/images/teriyaki.jpg"
}

POST /api/ingredients/create
{
  "name": "Dầu olive nguyên chất",
  "categoryId": "550e8400-e29b-41d4-a716-446655440004",
  "unit": "30ml",
  "unitPrice": 10000,
  "imageUrl": "https://example.com/images/olive-oil.jpg"
}

POST /api/ingredients/create
{
  "name": "Nước mắm chanh",
  "categoryId": "550e8400-e29b-41d4-a716-446655440004",
  "unit": "30ml",
  "unitPrice": 6000,
  "imageUrl": "https://example.com/images/fish-sauce.jpg"
}

// === INGREDIENTS CHO CATEGORY "TOPPING" ===

POST /api/ingredients/create
{
  "name": "Hạt điều rang",
  "categoryId": "550e8400-e29b-41d4-a716-446655440005",  // ID của category Topping
  "unit": "20g",
  "unitPrice": 8000,
  "imageUrl": "https://example.com/images/cashew.jpg"
}

POST /api/ingredients/create
{
  "name": "Phomat Feta",
  "categoryId": "550e8400-e29b-41d4-a716-446655440005",
  "unit": "30g",
  "unitPrice": 12000,
  "imageUrl": "https://example.com/images/feta.jpg"
}

POST /api/ingredients/create
{
  "name": "Hành tây ngâm",
  "categoryId": "550e8400-e29b-41d4-a716-446655440005",
  "unit": "20g",
  "unitPrice": 5000,
  "imageUrl": "https://example.com/images/pickled-onion.jpg"
}
```

**⚠️ LƯU Ý:**
- Mỗi ingredient phải thuộc một category
- `unitPrice` là giá cho 1 đơn vị (VD: 10000đ cho 100g)
- Lưu lại các `id` của Ingredients để dùng sau này

---

### BƯỚC 3: TẠO BOWL TEMPLATE (Mẫu bowl)

**Endpoint:** `POST /api/bowl_templates/create`

**Mục đích:** Tạo các mẫu bowl gợi ý cho khách hàng

**Ví dụ tạo Bowl Templates:**

```json
// Template 1: Mediterranean Bowl
POST /api/bowl_templates/create
{
  "name": "Mediterranean Bowl",
  "description": "Hương vị Địa Trung Hải với protein nướng, rau tươi và dầu olive",
  "imageUrl": "https://example.com/images/mediterranean-bowl.jpg"
}

// Template 2: Asian Fusion Bowl
POST /api/bowl_templates/create
{
  "name": "Asian Fusion Bowl",
  "description": "Sự kết hợp độc đáo của hương vị Á Đông hiện đại",
  "imageUrl": "https://example.com/images/asian-bowl.jpg"
}

// Template 3: Vegan Power Bowl
POST /api/bowl_templates/create
{
  "name": "Vegan Power Bowl",
  "description": "100% thuần chay, đầy đủ dinh dưỡng và năng lượng",
  "imageUrl": "https://example.com/images/vegan-bowl.jpg"
}

// Template 4: Keto Bowl
POST /api/bowl_templates/create
{
  "name": "Keto Bowl",
  "description": "Thấp carb, cao protein và chất béo lành mạnh",
  "imageUrl": "https://example.com/images/keto-bowl.jpg"
}
```

**Response mẫu:**
```json
{
  "code": 201,
  "message": "Bowl template created successfully",
  "data": {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "name": "Mediterranean Bowl",
    "description": "Hương vị Địa Trung Hải với protein nướng, rau tươi và dầu olive",
    "imageUrl": "https://example.com/images/mediterranean-bowl.jpg"
  }
}
```

**⚠️ LƯU Ý:** Lưu lại các `id` của Templates để tạo TemplateSteps!

---

### BƯỚC 4: TẠO TEMPLATE STEPS (Các bước cho template)

**Endpoint:** `POST /api/template_steps/create`

**Mục đích:** Định nghĩa các bước khách hàng sẽ thực hiện khi tạo bowl theo template

**Thuộc tính quan trọng:**
- `templateId`: ID của BowlTemplate
- `categoryId`: Chọn từ Category nào
- `minItems`: Số lượng nguyên liệu tối thiểu (0 = không bắt buộc)
- `maxItems`: Số lượng nguyên liệu tối đa
- `defaultQty`: Khối lượng mặc định cho mỗi nguyên liệu
- `displayOrder`: Thứ tự hiển thị (1, 2, 3, 4, 5...)

**Ví dụ: Tạo Steps cho "Mediterranean Bowl Template"**

```json
// STEP 1: Chọn Base (Bắt buộc 1 loại)
POST /api/template_steps/create
{
  "templateId": "660e8400-e29b-41d4-a716-446655440001",  // ID của Mediterranean Bowl
  "categoryId": "550e8400-e29b-41d4-a716-446655440001",  // ID của Category Base
  "minItems": 1,
  "maxItems": 1,
  "defaultQty": 200,
  "displayOrder": 1
}

// STEP 2: Chọn Protein (Bắt buộc 1-2 loại)
POST /api/template_steps/create
{
  "templateId": "660e8400-e29b-41d4-a716-446655440001",
  "categoryId": "550e8400-e29b-41d4-a716-446655440002",  // ID của Category Protein
  "minItems": 1,
  "maxItems": 2,
  "defaultQty": 100,
  "displayOrder": 2
}

// STEP 3: Chọn Veggies (Bắt buộc 2-5 loại)
POST /api/template_steps/create
{
  "templateId": "660e8400-e29b-41d4-a716-446655440001",
  "categoryId": "550e8400-e29b-41d4-a716-446655440003",  // ID của Category Veggie
  "minItems": 2,
  "maxItems": 5,
  "defaultQty": 50,
  "displayOrder": 3
}

// STEP 4: Chọn Sauce (Bắt buộc 1-2 loại)
POST /api/template_steps/create
{
  "templateId": "660e8400-e29b-41d4-a716-446655440001",
  "categoryId": "550e8400-e29b-41d4-a716-446655440004",  // ID của Category Sauce
  "minItems": 1,
  "maxItems": 2,
  "defaultQty": 30,
  "displayOrder": 4
}

// STEP 5: Chọn Topping (Tùy chọn, 0-3 loại)
POST /api/template_steps/create
{
  "templateId": "660e8400-e29b-41d4-a716-446655440001",
  "categoryId": "550e8400-e29b-41d4-a716-446655440005",  // ID của Category Topping
  "minItems": 0,
  "maxItems": 3,
  "defaultQty": 20,
  "displayOrder": 5
}
```

**Ví dụ: Tạo Steps cho "Keto Bowl Template"**

```json
// STEP 1: Chọn Base (Tùy chọn - Keto có thể không cần base)
POST /api/template_steps/create
{
  "templateId": "660e8400-e29b-41d4-a716-446655440004",  // ID của Keto Bowl
  "categoryId": "550e8400-e29b-41d4-a716-446655440001",
  "minItems": 0,
  "maxItems": 1,
  "defaultQty": 100,
  "displayOrder": 1
}

// STEP 2: Chọn Protein (Bắt buộc 2 loại cho Keto)
POST /api/template_steps/create
{
  "templateId": "660e8400-e29b-41d4-a716-446655440004",
  "categoryId": "550e8400-e29b-41d4-a716-446655440002",
  "minItems": 2,
  "maxItems": 3,
  "defaultQty": 150,
  "displayOrder": 2
}

// STEP 3: Chọn Veggies (Ít rau hơn cho Keto)
POST /api/template_steps/create
{
  "templateId": "660e8400-e29b-41d4-a716-446655440004",
  "categoryId": "550e8400-e29b-41d4-a716-446655440003",
  "minItems": 1,
  "maxItems": 3,
  "defaultQty": 30,
  "displayOrder": 3
}

// STEP 4: Chọn Sauce (Bắt buộc - nhưng phải là sauce ít carb)
POST /api/template_steps/create
{
  "templateId": "660e8400-e29b-41d4-a716-446655440004",
  "categoryId": "550e8400-e29b-41d4-a716-446655440004",
  "minItems": 1,
  "maxItems": 1,
  "defaultQty": 20,
  "displayOrder": 4
}
```

**💡 GIẢI THÍCH `displayOrder`:**

Frontend sẽ sắp xếp các steps theo `displayOrder` và hiển thị từng bước một:
- `displayOrder = 1`: Bước đầu tiên (hiển thị trước)
- `displayOrder = 2`: Bước thứ hai
- `displayOrder = 3`: Bước thứ ba
- ...

Khách hàng sẽ làm theo thứ tự này khi tạo bowl.

---

### BƯỚC 5 (TÙY CHỌN): TẠO INGREDIENT RESTRICTIONS

**Endpoint:** `POST /api/ingredient_restrictions/create`

**Mục đích:** Tạo các ràng buộc để ngăn việc kết hợp nguyên liệu không hợp lý

**Ví dụ:**

```json
// Không cho kết hợp Nước mắm với Phomat
POST /api/ingredient_restrictions/create
{
  "primaryIngredientId": "ingredient-nuoc-mam-id",
  "restrictedIngredientId": "ingredient-phomat-id",
  "reason": "Hương vị không hòa hợp"
}

// Không cho kết hợp Sữa với Hải sản
POST /api/ingredient_restrictions/create
{
  "primaryIngredientId": "ingredient-ca-hoi-id",
  "restrictedIngredientId": "ingredient-phomat-id",
  "reason": "Có thể gây khó tiêu"
}
```

---

## 👤 PHẦN 2: LUỒNG USER - ĐẶT MÓN

### 📋 THỨ TỰ THỰC HIỆN

```
1. Đăng ký/Đăng nhập
   ↓
2. Xem danh sách Bowl Templates
   ↓
3. Chọn 1 Template và xem chi tiết
   ↓
4. Tạo Order
   ↓
5. Tạo Bowl trong Order
   ↓
6. Thêm Ingredients vào Bowl (theo từng Step)
   ↓
7. Xem lại và Confirm Order
   ↓
8. Thanh toán
```

---

### BƯỚC 1: ĐĂNG KÝ / ĐĂNG NHẬP

#### 1.1. Đăng ký tài khoản

**Endpoint:** `POST /api/auth/register`

```json
POST /api/auth/register
{
  "email": "customer@example.com",
  "password": "Password123!",
  "fullName": "Nguyễn Văn A",
  "phone": "0901234567"
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Registered successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": "770e8400-e29b-41d4-a716-446655440001",
      "email": "customer@example.com",
      "fullName": "Nguyễn Văn A"
    }
  }
}
```

#### 1.2. Xác thực email (nếu cần)

**Endpoint:** `GET /api/auth/verify-email?token={token}`

```
GET /api/auth/verify-email?token=abc123xyz456
```

#### 1.3. Đăng nhập

**Endpoint:** `POST /api/auth/login`

```json
POST /api/auth/login
{
  "email": "customer@example.com",
  "password": "Password123!"
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": "770e8400-e29b-41d4-a716-446655440001",
      "email": "customer@example.com",
      "fullName": "Nguyễn Văn A"
    }
  }
}
```

**⚠️ LƯU Ý:** Lưu `accessToken` để gửi trong header của các request tiếp theo:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

### BƯỚC 2: XEM DANH SÁCH BOWL TEMPLATES

**Endpoint:** `GET /api/bowl_templates/getall`

```
GET /api/bowl_templates/getall
```

**Response:**
```json
{
  "code": 200,
  "message": "Bowl templates retrieved successfully",
  "data": [
    {
      "id": "660e8400-e29b-41d4-a716-446655440001",
      "name": "Mediterranean Bowl",
      "description": "Hương vị Địa Trung Hải với protein nướng, rau tươi và dầu olive",
      "imageUrl": "https://example.com/images/mediterranean-bowl.jpg"
    },
    {
      "id": "660e8400-e29b-41d4-a716-446655440002",
      "name": "Asian Fusion Bowl",
      "description": "Sự kết hợp độc đáo của hương vị Á Đông hiện đại",
      "imageUrl": "https://example.com/images/asian-bowl.jpg"
    },
    {
      "id": "660e8400-e29b-41d4-a716-446655440003",
      "name": "Vegan Power Bowl",
      "description": "100% thuần chay, đầy đủ dinh dưỡng và năng lượng",
      "imageUrl": "https://example.com/images/vegan-bowl.jpg"
    },
    {
      "id": "660e8400-e29b-41d4-a716-446655440004",
      "name": "Keto Bowl",
      "description": "Thấp carb, cao protein và chất béo lành mạnh",
      "imageUrl": "https://example.com/images/keto-bowl.jpg"
    }
  ]
}
```

---

### BƯỚC 3: XEM CHI TIẾT TEMPLATE

**Endpoint:** `GET /api/bowl_templates/getbyid/{id}`

```
GET /api/bowl_templates/getbyid/660e8400-e29b-41d4-a716-446655440001
```

**Response:**
```json
{
  "code": 200,
  "message": "Bowl template retrieved successfully",
  "data": {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "name": "Mediterranean Bowl",
    "description": "Hương vị Địa Trung Hải với protein nướng, rau tươi và dầu olive",
    "imageUrl": "https://example.com/images/mediterranean-bowl.jpg",
    "steps": [
      {
        "id": "step-001",
        "displayOrder": 1,
        "category": {
          "id": "550e8400-e29b-41d4-a716-446655440001",
          "name": "Base - Chọn nền",
          "kind": "CARB"
        },
        "minItems": 1,
        "maxItems": 1,
        "defaultQty": 200
      },
      {
        "id": "step-002",
        "displayOrder": 2,
        "category": {
          "id": "550e8400-e29b-41d4-a716-446655440002",
          "name": "Protein - Chọn món chính",
          "kind": "PROTEIN"
        },
        "minItems": 1,
        "maxItems": 2,
        "defaultQty": 100
      },
      {
        "id": "step-003",
        "displayOrder": 3,
        "category": {
          "id": "550e8400-e29b-41d4-a716-446655440003",
          "name": "Rau củ tươi",
          "kind": "VEGGIE"
        },
        "minItems": 2,
        "maxItems": 5,
        "defaultQty": 50
      },
      {
        "id": "step-004",
        "displayOrder": 4,
        "category": {
          "id": "550e8400-e29b-41d4-a716-446655440004",
          "name": "Nước sốt - Thêm hương vị",
          "kind": "SAUCE"
        },
        "minItems": 1,
        "maxItems": 2,
        "defaultQty": 30
      },
      {
        "id": "step-005",
        "displayOrder": 5,
        "category": {
          "id": "550e8400-e29b-41d4-a716-446655440005",
          "name": "Topping - Hoàn thiện",
          "kind": "TOPPING"
        },
        "minItems": 0,
        "maxItems": 3,
        "defaultQty": 20
      }
    ]
  }
}
```

**Frontend sẽ:**
1. Sort các `steps` theo `displayOrder`
2. Hiển thị từng bước một cho khách chọn

---

### BƯỚC 4: TẠO ORDER

**Endpoint:** `POST /api/orders/create`

```json
POST /api/orders/create
Headers: {
  "Authorization": "Bearer {accessToken}"
}
Body: {
  "storeId": "880e8400-e29b-41d4-a716-446655440001",
  "pickupAt": "2025-10-29T12:00:00Z",
  "note": "Không cay, ít dầu"
}
```

**Response:**
```json
{
  "code": 201,
  "message": "Order created successfully",
  "data": {
    "id": "990e8400-e29b-41d4-a716-446655440001",
    "storeId": "880e8400-e29b-41d4-a716-446655440001",
    "userId": "770e8400-e29b-41d4-a716-446655440001",
    "pickupAt": "2025-10-29T12:00:00Z",
    "status": "PENDING",
    "note": "Không cay, ít dầu",
    "subtotalAmount": 0,
    "promotionTotal": 0,
    "totalAmount": 0
  }
}
```

**⚠️ LƯU Ý:** Lưu lại `orderId` để tạo Bowl!

---

### BƯỚC 5: TẠO BOWL TRONG ORDER

**Endpoint:** `POST /api/bowls/create`

```json
POST /api/bowls/create
Headers: {
  "Authorization": "Bearer {accessToken}"
}
Body: {
  "orderId": "990e8400-e29b-41d4-a716-446655440001",
  "templateId": "660e8400-e29b-41d4-a716-446655440001",
  "name": "My Mediterranean Bowl",
  "instruction": "Nước sốt ít thôi"
}
```

**Response:**
```json
{
  "code": 201,
  "message": "Bowl created successfully",
  "data": {
    "id": "aa0e8400-e29b-41d4-a716-446655440001",
    "orderId": "990e8400-e29b-41d4-a716-446655440001",
    "templateId": "660e8400-e29b-41d4-a716-446655440001",
    "name": "My Mediterranean Bowl",
    "instruction": "Nước sốt ít thôi",
    "linePrice": 0,
    "items": []
  }
}
```

**⚠️ LƯU Ý:** Lưu lại `bowlId` để thêm ingredients!

---

### BƯỚC 6: THÊM INGREDIENTS VÀO BOWL

**Endpoint:** `POST /api/bowl_items/create`

Khách hàng sẽ chọn ingredients theo từng step (theo `displayOrder`):

#### Step 1: Chọn Base (min=1, max=1)

```json
POST /api/bowl_items/create
Headers: {
  "Authorization": "Bearer {accessToken}"
}
Body: {
  "bowlId": "aa0e8400-e29b-41d4-a716-446655440001",
  "ingredientId": "ingredient-com-gao-lut-id",
  "quantity": 200
}
```

**Response:**
```json
{
  "code": 201,
  "message": "Bowl item created successfully",
  "data": {
    "id": "item-001",
    "bowlId": "aa0e8400-e29b-41d4-a716-446655440001",
    "ingredientId": "ingredient-com-gao-lut-id",
    "ingredientName": "Cơm gạo lứt",
    "quantity": 200,
    "unitPrice": 10000
  }
}
```

#### Step 2: Chọn Protein (min=1, max=2)

```json
// Chọn Protein #1
POST /api/bowl_items/create
{
  "bowlId": "aa0e8400-e29b-41d4-a716-446655440001",
  "ingredientId": "ingredient-uc-ga-nuong-id",
  "quantity": 100
}

// Chọn Protein #2 (tùy chọn)
POST /api/bowl_items/create
{
  "bowlId": "aa0e8400-e29b-41d4-a716-446655440001",
  "ingredientId": "ingredient-ca-hoi-id",
  "quantity": 50
}
```

#### Step 3: Chọn Veggies (min=2, max=5)

```json
// Veggie #1
POST /api/bowl_items/create
{
  "bowlId": "aa0e8400-e29b-41d4-a716-446655440001",
  "ingredientId": "ingredient-xa-lach-id",
  "quantity": 50
}

// Veggie #2
POST /api/bowl_items/create
{
  "bowlId": "aa0e8400-e29b-41d4-a716-446655440001",
  "ingredientId": "ingredient-ca-chua-id",
  "quantity": 50
}

// Veggie #3
POST /api/bowl_items/create
{
  "bowlId": "aa0e8400-e29b-41d4-a716-446655440001",
  "ingredientId": "ingredient-dua-chuot-id",
  "quantity": 50
}
```

#### Step 4: Chọn Sauce (min=1, max=2)

```json
POST /api/bowl_items/create
{
  "bowlId": "aa0e8400-e29b-41d4-a716-446655440001",
  "ingredientId": "ingredient-dau-olive-id",
  "quantity": 30
}
```

#### Step 5: Chọn Topping (min=0, max=3) - Tùy chọn

```json
// Topping #1
POST /api/bowl_items/create
{
  "bowlId": "aa0e8400-e29b-41d4-a716-446655440001",
  "ingredientId": "ingredient-phomat-feta-id",
  "quantity": 30
}

// Topping #2
POST /api/bowl_items/create
{
  "bowlId": "aa0e8400-e29b-41d4-a716-446655440001",
  "ingredientId": "ingredient-hat-dieu-id",
  "quantity": 20
}
```

**❌ NẾU VI PHẠM INGREDIENT RESTRICTION:**

```json
{
  "code": 400,
  "errorCode": "INGREDIENT_RESTRICTION_VIOLATED",
  "message": "Không thể kết hợp Cá hồi với Phomat: Có thể gây khó tiêu"
}
```

---

### BƯỚC 7: XEM LẠI BOWL

**Endpoint:** `GET /api/bowls/getbyid/{id}`

```
GET /api/bowls/getbyid/aa0e8400-e29b-41d4-a716-446655440001
```

**Response:**
```json
{
  "code": 200,
  "message": "Bowl retrieved successfully",
  "data": {
    "id": "aa0e8400-e29b-41d4-a716-446655440001",
    "orderId": "990e8400-e29b-41d4-a716-446655440001",
    "templateName": "Mediterranean Bowl",
    "name": "My Mediterranean Bowl",
    "instruction": "Nước sốt ít thôi",
    "linePrice": 98000,
    "items": [
      {
        "ingredientName": "Cơm gạo lứt",
        "quantity": 200,
        "unitPrice": 10000,
        "totalPrice": 20000
      },
      {
        "ingredientName": "Ức gà nướng",
        "quantity": 100,
        "unitPrice": 25000,
        "totalPrice": 25000
      },
      {
        "ingredientName": "Cá hồi nướng",
        "quantity": 50,
        "unitPrice": 45000,
        "totalPrice": 22500
      },
      {
        "ingredientName": "Xà lách",
        "quantity": 50,
        "unitPrice": 5000,
        "totalPrice": 5000
      },
      {
        "ingredientName": "Cà chua cherry",
        "quantity": 50,
        "unitPrice": 6000,
        "totalPrice": 6000
      },
      {
        "ingredientName": "Dưa chuột",
        "quantity": 50,
        "unitPrice": 4000,
        "totalPrice": 4000
      },
      {
        "ingredientName": "Dầu olive nguyên chất",
        "quantity": 30,
        "unitPrice": 10000,
        "totalPrice": 10000
      },
      {
        "ingredientName": "Hạt điều rang",
        "quantity": 20,
        "unitPrice": 8000,
        "totalPrice": 5600
      }
    ]
  }
}
```

**Tính toán:**
- Cơm gạo lứt: 200g × 10,000đ/100g = 20,000đ
- Ức gà nướng: 100g × 25,000đ/100g = 25,000đ
- Cá hồi: 50g × 45,000đ/100g = 22,500đ
- Xà lách: 50g × 5,000đ/50g = 5,000đ
- Cà chua: 50g × 6,000đ/50g = 6,000đ
- Dưa chuột: 50g × 4,000đ/50g = 4,000đ
- Dầu olive: 30ml × 10,000đ/30ml = 10,000đ
- Hạt điều: 20g × 8,000đ/20g = 8,000đ

**Tổng: 100,500đ**

---

### BƯỚC 8: XEM LẠI ORDER VÀ CONFIRM

**Endpoint:** `GET /api/orders/getbyid/{id}`

```
GET /api/orders/getbyid/990e8400-e29b-41d4-a716-446655440001
```

**Response:**
```json
{
  "code": 200,
  "message": "Order retrieved successfully",
  "data": {
    "id": "990e8400-e29b-41d4-a716-446655440001",
    "status": "PENDING",
    "pickupAt": "2025-10-29T12:00:00Z",
    "note": "Không cay, ít dầu",
    "subtotalAmount": 100500,
    "promotionTotal": 0,
    "totalAmount": 100500,
    "bowls": [
      {
        "id": "aa0e8400-e29b-41d4-a716-446655440001",
        "name": "My Mediterranean Bowl",
        "linePrice": 100500
      }
    ]
  }
}
```

**Confirm Order:**

**Endpoint:** `PUT /api/orders/update/{id}`

```json
PUT /api/orders/update/990e8400-e29b-41d4-a716-446655440001
Headers: {
  "Authorization": "Bearer {accessToken}"
}
Body: {
  "status": "CONFIRMED"
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Order updated successfully",
  "data": {
    "id": "990e8400-e29b-41d4-a716-446655440001",
    "status": "CONFIRMED",
    "totalAmount": 100500
  }
}
```

---

### BƯỚC 9: THANH TOÁN

**Endpoint:** `POST /api/payment_transactions/create`

```json
POST /api/payment_transactions/create
Headers: {
  "Authorization": "Bearer {accessToken}"
}
Body: {
  "orderId": "990e8400-e29b-41d4-a716-446655440001",
  "method": "VNPAY",
  "amount": 100500
}
```

**Response:**
```json
{
  "code": 201,
  "message": "Payment transaction created successfully",
  "data": {
    "id": "payment-001",
    "orderId": "990e8400-e29b-41d4-a716-446655440001",
    "method": "VNPAY",
    "amount": 100500,
    "status": "PENDING",
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?..."
  }
}
```

Khách hàng sẽ được redirect đến `paymentUrl` để thanh toán.

---

## 📊 TỔNG KẾT LUỒNG

### ADMIN:
```
1. Tạo Categories (Base, Protein, Veggie, Sauce, Topping)
2. Tạo Ingredients cho từng Category
3. Tạo BowlTemplates (Mediterranean, Asian, Vegan, Keto...)
4. Tạo TemplateSteps cho từng Template (định nghĩa min/max/displayOrder)
5. (Optional) Tạo IngredientRestrictions
```

### USER:
```
1. Đăng ký/Đăng nhập
2. Xem danh sách Templates
3. Chọn 1 Template
4. Tạo Order
5. Tạo Bowl trong Order
6. Thêm Ingredients theo từng Step:
   - Step 1 (displayOrder=1): Chọn Base
   - Step 2 (displayOrder=2): Chọn Protein
   - Step 3 (displayOrder=3): Chọn Veggies
   - Step 4 (displayOrder=4): Chọn Sauce
   - Step 5 (displayOrder=5): Chọn Toppings
7. Xem lại Bowl (kiểm tra giá)
8. Confirm Order
9. Thanh toán
```

---

## ⚠️ LƯU Ý QUAN TRỌNG

### Validation Rules:
1. **TemplateStep min/max:** Phải chọn đúng số lượng ingredients theo `minItems` và `maxItems`
2. **IngredientRestriction:** Không được kết hợp các ingredients bị cấm
3. **Order Status:** Chỉ có thể thanh toán khi Order ở trạng thái `CONFIRMED`

### Authorization:
- Tất cả các endpoints của User đều cần `Authorization: Bearer {token}`
- Admin endpoints cần role `ADMIN`

### Error Handling:
- `404 NOT_FOUND`: Resource không tồn tại
- `400 BAD_REQUEST`: Dữ liệu không hợp lệ
- `400 INGREDIENT_RESTRICTION_VIOLATED`: Vi phạm ràng buộc nguyên liệu
- `401 UNAUTHORIZED`: Chưa đăng nhập
- `403 FORBIDDEN`: Không có quyền

---

## 🎉 KẾT LUẬN

Hệ thống được thiết kế linh hoạt, cho phép:
- ✅ Admin dễ dàng quản lý menu và templates
- ✅ User tự do customize bowl theo sở thích
- ✅ Validation chặt chẽ đảm bảo chất lượng
- ✅ Tính giá tự động và chính xác
- ✅ Hỗ trợ nhiều phương thức thanh toán

**Chúc bạn sử dụng hệ thống thành công! 🚀**

