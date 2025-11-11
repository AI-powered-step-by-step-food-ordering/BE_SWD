# Search Capability Analysis - Entities & Controllers

## Date: November 12, 2025

Phân tích chi tiết các entity và controller để xác định khả năng tích hợp tính năng **search/filter** vào các endpoint GET dựa trên phân tích sorting capability.

---

## 📊 Summary

| Entity | Search Priority | Recommended Search Fields | Search Type | Use Case |
|--------|----------------|---------------------------|-------------|----------|
| **Order** | 🔥 **CRITICAL** | id, status, userId, storeId, totalAmount (range), pickupAt (range) | Exact + Range + Date | Order management, User history |
| **User** | 🔥 **CRITICAL** | email, fullName, phone, role, status | Exact + Partial | User management, Admin search |
| **Promotion** | 🔥 **HIGH** | code, name, type, status (active/expired) | Exact + Partial | Promotion lookup |
| **Ingredient** | 🔥 **HIGH** | name, categoryId, unitPrice (range) | Partial + Range | Menu builder, Inventory |
| **PaymentTransaction** | 🔴 **MEDIUM-HIGH** | id, orderId, status, method, amount (range), capturedAt (range) | Exact + Range | Transaction history |
| **Store** | 🔴 **MEDIUM-HIGH** | name, address, phone | Partial | Store lookup |
| **KitchenJob** | 🔴 **MEDIUM-HIGH** | orderId, status, assignedUserId | Exact | Kitchen workflow |
| **Notification** | 🟡 **MEDIUM** | userId, type, readAt (null/not null), orderStatus | Exact + Boolean | User inbox |
| **Bowl** | 🟡 **MEDIUM** | name, orderId, templateId | Partial + Exact | Order details |
| **BowlTemplate** | 🟡 **MEDIUM** | name | Partial | Template selection |
| **Category** | 🟢 **LOW** | name, kind | Partial + Exact | Category browsing |
| **Inventory** | 🔴 **MEDIUM-HIGH** | ingredientId, storeId, action, createdAt (range) | Exact + Range | Stock tracking |
| **PromotionRedemption** | 🟢 **LOW** | promotionId, orderId, userId, status | Exact | Analytics |

---

## 🔥 CRITICAL PRIORITY Entities (Phải có Search ngay)

### 1. **Order** 📦
**Controller:** `OrderController.java`  
**Endpoints:**
- `GET /api/orders/getall`
- `GET /api/orders/order-history/{userId}` (đã filter by userId)

**Entity Fields:**
```java
- id: String ⭐ SEARCHABLE (exact match)
- pickupAt: OffsetDateTime ⭐ SEARCHABLE (date range)
- status: OrderStatus ⭐ SEARCHABLE (exact match, multi-select)
- subtotalAmount: Double ⭐ SEARCHABLE (range: min-max)
- totalAmount: Double ⭐ SEARCHABLE (range: min-max)
- user.id: String ⭐ SEARCHABLE (exact match)
- store.id: String ⭐ SEARCHABLE (exact match)
- createdAt: ZonedDateTime ⭐ SEARCHABLE (date range)
```

**Recommended Search Parameters:**
```
# 1. Search by Status (single or multiple)
GET /api/orders/getall?status=PENDING
GET /api/orders/getall?status=PENDING,CONFIRMED,PREPARING

# 2. Search by User
GET /api/orders/getall?userId=user-uuid-123

# 3. Search by Store
GET /api/orders/getall?storeId=store-uuid-456

# 4. Search by Date Range
GET /api/orders/getall?createdFrom=2025-11-01&createdTo=2025-11-30
GET /api/orders/getall?pickupFrom=2025-11-12T08:00:00&pickupTo=2025-11-12T18:00:00

# 5. Search by Amount Range
GET /api/orders/getall?minAmount=50000&maxAmount=200000

# 6. Combined Search + Sort + Pagination
GET /api/orders/getall?status=COMPLETED&userId=user-123&minAmount=100000&sort=createdAt,desc&page=0&size=10

# 7. Search by Order ID (exact match)
GET /api/orders/getall?orderId=order-uuid-789
```

**Use Cases:**
- ✅ Admin filter orders by status (PENDING, COMPLETED, CANCELLED)
- ✅ User xem lịch sử đơn hàng theo khoảng thời gian
- ✅ Manager xem orders theo store
- ✅ Filter orders theo giá trị đơn hàng
- ✅ Tìm kiếm order bằng ID (cho support team)

**Implementation Priority:** **P0 - Must Have**

---

### 2. **User** 👤
**Controller:** `UserController.java` (extends BaseController)  
**Endpoints:**
- `GET /api/users/getall`
- `GET /api/users/active`
- `GET /api/users/inactive`

**Entity Fields:**
```java
- id: String ⭐ SEARCHABLE (exact match)
- fullName: String ⭐ SEARCHABLE (partial match, case-insensitive)
- email: String ⭐ SEARCHABLE (partial match, case-insensitive)
- phone: String ⭐ SEARCHABLE (partial match)
- role: Role ⭐ SEARCHABLE (exact match, multi-select)
- status: AccountStatus ⭐ SEARCHABLE (exact match)
- emailVerified: Boolean ⭐ SEARCHABLE (boolean)
- createdAt: ZonedDateTime ⭐ SEARCHABLE (date range)
```

**Recommended Search Parameters:**
```
# 1. Search by Name (partial, case-insensitive)
GET /api/users/getall?name=nguyen
GET /api/users/getall?fullName=Nguyen Van

# 2. Search by Email (partial or exact)
GET /api/users/getall?email=@gmail.com
GET /api/users/getall?email=john@example.com

# 3. Search by Phone
GET /api/users/getall?phone=0912345678

# 4. Search by Role
GET /api/users/getall?role=STAFF
GET /api/users/getall?role=ADMIN,MANAGER

# 5. Search by Status
GET /api/users/getall?status=ACTIVE
GET /api/users/getall?status=SUSPENDED

# 6. Search by Email Verification Status
GET /api/users/getall?emailVerified=false

# 7. Combined Search
GET /api/users/getall?role=USER&status=ACTIVE&emailVerified=true&sort=createdAt,desc

# 8. Search by Registration Date
GET /api/users/getall?createdFrom=2025-01-01&createdTo=2025-12-31
```

**Use Cases:**
- ✅ Admin tìm user theo tên hoặc email
- ✅ Support team tìm user theo số điện thoại
- ✅ Filter users theo role (quản lý staff)
- ✅ Tìm users chưa verify email
- ✅ Tìm users bị suspended

**Implementation Priority:** **P0 - Must Have**

---

## 🔥 HIGH PRIORITY Entities

### 3. **Promotion** 🎁
**Controller:** `PromotionController.java` (extends BaseController)  
**Endpoints:**
- `GET /api/promotions/getall`
- `GET /api/promotions/active`

**Entity Fields:**
```java
- id: String ⭐ SEARCHABLE (exact match)
- code: String ⭐ SEARCHABLE (exact match, case-insensitive)
- name: String ⭐ SEARCHABLE (partial match)
- type: PromotionType ⭐ SEARCHABLE (exact match)
- startsAt: OffsetDateTime ⭐ SEARCHABLE (date range)
- endsAt: OffsetDateTime ⭐ SEARCHABLE (date range)
- percentOff: BigDecimal ⭐ SEARCHABLE (range)
- amountOff: BigDecimal ⭐ SEARCHABLE (range)
- status: [Computed] ⭐ SEARCHABLE (active, expired, upcoming)
```

**Recommended Search Parameters:**
```
# 1. Search by Code (exact match)
GET /api/promotions/getall?code=SUMMER2025

# 2. Search by Name (partial)
GET /api/promotions/getall?name=summer

# 3. Search by Type
GET /api/promotions/getall?type=PERCENT_OFF
GET /api/promotions/getall?type=AMOUNT_OFF

# 4. Search by Status (computed based on dates)
GET /api/promotions/active  # startsAt <= now AND endsAt >= now
GET /api/promotions/getall?status=upcoming  # startsAt > now
GET /api/promotions/getall?status=expired   # endsAt < now

# 5. Search by Discount Value
GET /api/promotions/getall?minPercentOff=10&maxPercentOff=50
GET /api/promotions/getall?minAmountOff=10000

# 6. Search by Date Range
GET /api/promotions/getall?startsFrom=2025-11-01&startsTo=2025-11-30

# 7. Combined Search
GET /api/promotions/getall?type=PERCENT_OFF&status=active&minPercentOff=20
```

**Use Cases:**
- ✅ Admin tìm promotion bằng code
- ✅ User tìm promotions đang active
- ✅ Marketing team filter by type và discount value
- ✅ Tìm promotions sắp hết hạn

**Implementation Priority:** **P0 - Must Have**

---

### 4. **Ingredient** 🥬
**Controller:** `IngredientController.java` (extends BaseController)  
**Endpoints:**
- `GET /api/ingredients/getall`
- `GET /api/ingredients/active`

**Entity Fields:**
```java
- id: String ⭐ SEARCHABLE (exact match)
- name: String ⭐ SEARCHABLE (partial match, case-insensitive)
- category.id: String ⭐ SEARCHABLE (exact match)
- category.kind: IngredientKind ⭐ SEARCHABLE (exact match)
- unitPrice: Double ⭐ SEARCHABLE (range)
- unit: String ⭐ SEARCHABLE (exact match)
```

**Recommended Search Parameters:**
```
# 1. Search by Name (partial, case-insensitive)
GET /api/ingredients/getall?name=chicken
GET /api/ingredients/getall?name=rau

# 2. Search by Category
GET /api/ingredients/getall?categoryId=category-uuid-123

# 3. Search by Kind (via Category)
GET /api/ingredients/getall?kind=PROTEIN
GET /api/ingredients/getall?kind=VEGGIE,TOPPING

# 4. Search by Price Range
GET /api/ingredients/getall?minPrice=5000&maxPrice=50000

# 5. Search by Unit
GET /api/ingredients/getall?unit=gram

# 6. Combined Search
GET /api/ingredients/getall?kind=PROTEIN&minPrice=20000&sort=unitPrice,asc

# 7. Active ingredients only
GET /api/ingredients/active?name=chicken&kind=PROTEIN
```

**Use Cases:**
- ✅ Menu builder: tìm ingredient theo tên
- ✅ Filter theo category/kind (BASE, PROTEIN, VEGGIE, etc.)
- ✅ Budget planning: filter theo giá
- ✅ Inventory: tìm items theo unit

**Implementation Priority:** **P1 - High**

---

## 🔴 MEDIUM-HIGH PRIORITY Entities

### 5. **PaymentTransaction** 💳
**Controller:** `PaymentTransactionController.java`  
**Endpoints:**
- `GET /api/payment_transactions/getall`
- `GET /api/payment_transactions/payment-history/{userId}`

**Entity Fields:**
```java
- id: String ⭐ SEARCHABLE (exact match)
- order.id: String ⭐ SEARCHABLE (exact match)
- method: PaymentMethod ⭐ SEARCHABLE (exact match)
- status: PaymentStatus ⭐ SEARCHABLE (exact match)
- amount: Double ⭐ SEARCHABLE (range)
- providerTxnId: String ⭐ SEARCHABLE (exact match)
- capturedAt: OffsetDateTime ⭐ SEARCHABLE (date range)
- createdAt: ZonedDateTime ⭐ SEARCHABLE (date range)
```

**Recommended Search Parameters:**
```
# 1. Search by Status
GET /api/payment_transactions/getall?status=SUCCESS
GET /api/payment_transactions/getall?status=PENDING,FAILED

# 2. Search by Payment Method
GET /api/payment_transactions/getall?method=ZALOPAY
GET /api/payment_transactions/getall?method=CASH,ZALOPAY

# 3. Search by Order ID
GET /api/payment_transactions/getall?orderId=order-uuid-123

# 4. Search by Amount Range
GET /api/payment_transactions/getall?minAmount=100000&maxAmount=500000

# 5. Search by Date Range
GET /api/payment_transactions/getall?capturedFrom=2025-11-01&capturedTo=2025-11-30

# 6. Search by Provider Transaction ID
GET /api/payment_transactions/getall?providerTxnId=ZP-123456789

# 7. Combined Search
GET /api/payment_transactions/getall?status=SUCCESS&method=ZALOPAY&minAmount=50000&sort=capturedAt,desc
```

**Use Cases:**
- ✅ Finance team: filter transactions theo status
- ✅ Support: tìm transaction theo provider ID
- ✅ Report: filter theo payment method và date range
- ✅ Audit: tìm failed transactions

**Implementation Priority:** **P1 - High**

---

### 6. **Store** 🏪
**Controller:** `StoreController.java` (extends BaseController)  
**Endpoints:**
- `GET /api/stores/getall`
- `GET /api/stores/active`

**Entity Fields:**
```java
- id: String ⭐ SEARCHABLE (exact match)
- name: String ⭐ SEARCHABLE (partial match, case-insensitive)
- address: String ⭐ SEARCHABLE (partial match, case-insensitive)
- phone: String ⭐ SEARCHABLE (partial match)
```

**Recommended Search Parameters:**
```
# 1. Search by Name (partial)
GET /api/stores/getall?name=downtown

# 2. Search by Address (partial)
GET /api/stores/getall?address=Ho Chi Minh

# 3. Search by Phone
GET /api/stores/getall?phone=028

# 4. Combined Search
GET /api/stores/active?address=District 1&sort=name,asc
```

**Use Cases:**
- ✅ User tìm store gần địa chỉ
- ✅ Admin quản lý stores
- ✅ Support tìm store theo phone

**Implementation Priority:** **P2 - Medium**

---

### 7. **KitchenJob** 👨‍🍳
**Controller:** `KitchenJobController.java`  
**Endpoint:** `GET /api/kitchen_jobs/getall`

**Entity Fields:**
```java
- id: String ⭐ SEARCHABLE (exact match)
- order.id: String ⭐ SEARCHABLE (exact match)
- status: JobStatus ⭐ SEARCHABLE (exact match)
- assignedUser.id: String ⭐ SEARCHABLE (exact match)
- startedAt: OffsetDateTime ⭐ SEARCHABLE (date range)
- finishedAt: OffsetDateTime ⭐ SEARCHABLE (date range)
- createdAt: ZonedDateTime ⭐ SEARCHABLE (date range)
```

**Recommended Search Parameters:**
```
# 1. Search by Status
GET /api/kitchen_jobs/getall?status=QUEUED
GET /api/kitchen_jobs/getall?status=IN_PROGRESS,QUEUED

# 2. Search by Assigned User (Staff)
GET /api/kitchen_jobs/getall?assignedUserId=staff-uuid-123

# 3. Search by Order ID
GET /api/kitchen_jobs/getall?orderId=order-uuid-456

# 4. Search by Date Range
GET /api/kitchen_jobs/getall?createdFrom=2025-11-12T00:00:00&createdTo=2025-11-12T23:59:59

# 5. Combined Search
GET /api/kitchen_jobs/getall?status=IN_PROGRESS&assignedUserId=staff-123&sort=createdAt,asc
```

**Use Cases:**
- ✅ Kitchen staff xem jobs assigned to them
- ✅ Manager track jobs by status
- ✅ Performance metrics: finished jobs in date range

**Implementation Priority:** **P1 - High**

---

### 8. **Inventory** 📊
**Controller:** `InventoryController.java`  
**Endpoint:** `GET /api/inventories/getall`

**Entity Fields:**
```java
- id: String ⭐ SEARCHABLE (exact match)
- ingredient.id: String ⭐ SEARCHABLE (exact match)
- store.id: String ⭐ SEARCHABLE (exact match)
- action: StockAction ⭐ SEARCHABLE (exact match)
- quantityChange: Double ⭐ SEARCHABLE (range)
- balanceAfter: Double ⭐ SEARCHABLE (range)
- createdAt: ZonedDateTime ⭐ SEARCHABLE (date range)
```

**Recommended Search Parameters:**
```
# 1. Search by Ingredient
GET /api/inventories/getall?ingredientId=ingredient-uuid-123

# 2. Search by Store
GET /api/inventories/getall?storeId=store-uuid-456

# 3. Search by Action Type
GET /api/inventories/getall?action=IN
GET /api/inventories/getall?action=OUT,ADJUST

# 4. Search by Date Range
GET /api/inventories/getall?createdFrom=2025-11-01&createdTo=2025-11-30

# 5. Low Stock Alert
GET /api/inventories/getall?maxBalance=10

# 6. Combined Search
GET /api/inventories/getall?ingredientId=ing-123&storeId=store-456&action=OUT&sort=createdAt,desc
```

**Use Cases:**
- ✅ Track inventory changes for specific ingredient
- ✅ Audit trail by store
- ✅ Low stock alerts
- ✅ Filter by action type (IN/OUT/ADJUST)

**Implementation Priority:** **P1 - High**

---

## 🟡 MEDIUM PRIORITY Entities

### 9. **Notification** 🔔
**Controller:** `NotificationController.java`  
**Endpoints:** (Need to check if exist)

**Entity Fields:**
```java
- id: String ⭐ SEARCHABLE (exact match)
- user.id: String ⭐ SEARCHABLE (exact match)
- order.id: String ⭐ SEARCHABLE (exact match)
- type: NotificationType ⭐ SEARCHABLE (exact match)
- orderStatus: OrderStatus ⭐ SEARCHABLE (exact match)
- readAt: OffsetDateTime ⭐ SEARCHABLE (null/not null)
- sentAt: OffsetDateTime ⭐ SEARCHABLE (date range)
- deliverySuccess: Boolean ⭐ SEARCHABLE (boolean)
```

**Recommended Search Parameters:**
```
# 1. Search by User (already filtered in most cases)
GET /api/notifications/getall?userId=user-uuid-123

# 2. Search Unread Notifications
GET /api/notifications/getall?unread=true  # readAt IS NULL
GET /api/notifications/getall?read=true    # readAt IS NOT NULL

# 3. Search by Type
GET /api/notifications/getall?type=ORDER_STATUS_UPDATE
GET /api/notifications/getall?type=PROMOTION,SYSTEM

# 4. Search by Order Status
GET /api/notifications/getall?orderStatus=READY

# 5. Search by Date Range
GET /api/notifications/getall?sentFrom=2025-11-01&sentTo=2025-11-30

# 6. Search Failed Deliveries
GET /api/notifications/getall?deliverySuccess=false

# 7. Combined Search
GET /api/notifications/getall?userId=user-123&unread=true&type=ORDER_STATUS_UPDATE&sort=sentAt,desc
```

**Use Cases:**
- ✅ User xem unread notifications
- ✅ Filter by notification type
- ✅ Support: debug failed deliveries
- ✅ Filter notifications by order status

**Implementation Priority:** **P2 - Medium**

---

### 10. **Bowl** 🥗
**Controller:** `BowlController.java`  
**Endpoint:** `GET /api/bowls/getall`

**Entity Fields:**
```java
- id: String ⭐ SEARCHABLE (exact match)
- name: String ⭐ SEARCHABLE (partial match)
- order.id: String ⭐ SEARCHABLE (exact match)
- template.id: String ⭐ SEARCHABLE (exact match)
- linePrice: Double ⭐ SEARCHABLE (range)
```

**Recommended Search Parameters:**
```
# 1. Search by Name
GET /api/bowls/getall?name=chicken

# 2. Search by Order
GET /api/bowls/getall?orderId=order-uuid-123

# 3. Search by Template
GET /api/bowls/getall?templateId=template-uuid-456

# 4. Search by Price Range
GET /api/bowls/getall?minPrice=50000&maxPrice=150000
```

**Implementation Priority:** **P3 - Low** (Usually filtered by Order)

---

### 11. **BowlTemplate** 📋
**Controller:** `BowlTemplateController.java` (extends BaseController)  
**Endpoints:**
- `GET /api/bowl_templates/getall`
- `GET /api/bowl_templates/active`

**Entity Fields:**
```java
- id: String ⭐ SEARCHABLE (exact match)
- name: String ⭐ SEARCHABLE (partial match, case-insensitive)
- description: String ⭐ SEARCHABLE (partial match)
```

**Recommended Search Parameters:**
```
# 1. Search by Name (partial)
GET /api/bowl_templates/getall?name=healthy

# 2. Search by Description
GET /api/bowl_templates/getall?description=vegan

# 3. Active templates only
GET /api/bowl_templates/active?name=bowl
```

**Use Cases:**
- ✅ User tìm template theo tên
- ✅ Filter active templates

**Implementation Priority:** **P2 - Medium**

---

### 12. **Category** 📂
**Controller:** `CategoryController.java` (extends BaseController)  
**Endpoints:**
- `GET /api/categories/getall`
- `GET /api/categories/active`

**Entity Fields:**
```java
- id: String ⭐ SEARCHABLE (exact match)
- name: String ⭐ SEARCHABLE (partial match)
- kind: IngredientKind ⭐ SEARCHABLE (exact match)
```

**Recommended Search Parameters:**
```
# 1. Search by Name
GET /api/categories/getall?name=protein

# 2. Search by Kind
GET /api/categories/getall?kind=PROTEIN
GET /api/categories/getall?kind=VEGGIE,TOPPING

# 3. Combined Search
GET /api/categories/active?kind=BASE&sort=name,asc
```

**Implementation Priority:** **P3 - Low** (Small dataset)

---

### 13. **PromotionRedemption** 🎫
**Controller:** `PromotionRedemptionController.java`  
**Endpoint:** `GET /api/promotion_redemptions/getall`

**Entity Fields:**
```java
- id: String ⭐ SEARCHABLE (exact match)
- promotion.id: String ⭐ SEARCHABLE (exact match)
- order.id: String ⭐ SEARCHABLE (exact match)
- user.id: String ⭐ SEARCHABLE (exact match)
- status: RedemptionStatus ⭐ SEARCHABLE (exact match)
- createdAt: ZonedDateTime ⭐ SEARCHABLE (date range)
```

**Recommended Search Parameters:**
```
# 1. Search by Promotion
GET /api/promotion_redemptions/getall?promotionId=promo-uuid-123

# 2. Search by User
GET /api/promotion_redemptions/getall?userId=user-uuid-456

# 3. Search by Order
GET /api/promotion_redemptions/getall?orderId=order-uuid-789

# 4. Search by Status
GET /api/promotion_redemptions/getall?status=APPLIED
GET /api/promotion_redemptions/getall?status=CANCELLED

# 5. Search by Date Range
GET /api/promotion_redemptions/getall?createdFrom=2025-11-01&createdTo=2025-11-30
```

**Use Cases:**
- ✅ Analytics: track promotion usage
- ✅ User redemption history

**Implementation Priority:** **P3 - Low** (Analytics only)

---

## 🎯 Implementation Recommendations

### Phase 1: CRITICAL (P0 - Must Have) 🔥
1. ✅ **Order** - Multi-field search (status, user, store, date, amount)
2. ✅ **User** - Name, email, phone, role search
3. ✅ **Promotion** - Code, name, status search

**Timeline:** Sprint 1 (Week 1-2)

---

### Phase 2: HIGH PRIORITY (P1) 🔴
4. ✅ **Ingredient** - Name, category, price search
5. ✅ **PaymentTransaction** - Status, method, amount search
6. ✅ **KitchenJob** - Status, assigned user search
7. ✅ **Inventory** - Ingredient, store, action search

**Timeline:** Sprint 2 (Week 3-4)

---

### Phase 3: MEDIUM PRIORITY (P2) 🟡
8. ✅ **Store** - Name, address, phone search
9. ✅ **Notification** - Type, read/unread search
10. ✅ **BowlTemplate** - Name search

**Timeline:** Sprint 3 (Week 5-6)

---

### Phase 4: LOW PRIORITY (P3) 🟢
11. ✅ **Bowl** - Name, order, template search
12. ✅ **Category** - Name, kind search
13. ✅ **PromotionRedemption** - Promotion, user, status search

**Timeline:** Sprint 4 (Week 7-8)

---

## 📋 Search Parameter Format Recommendations

### 1. **Exact Match Search**
```
GET /api/{entity}/getall?{field}={value}

Examples:
- ?status=PENDING
- ?role=ADMIN
- ?userId=user-uuid-123
```

### 2. **Partial Match Search (String fields)**
```
GET /api/{entity}/getall?{field}={value}

Examples:
- ?name=nguyen           # LIKE %nguyen%
- ?email=@gmail.com      # LIKE %@gmail.com%
- ?phone=091             # LIKE %091%
```

**Case-Insensitive:** `LOWER(field) LIKE LOWER('%value%')`

### 3. **Multi-Value Search (OR condition)**
```
GET /api/{entity}/getall?{field}={value1},{value2},{value3}

Examples:
- ?status=PENDING,CONFIRMED,PREPARING
- ?role=ADMIN,MANAGER
- ?kind=PROTEIN,VEGGIE
```

### 4. **Range Search (Numeric/Date fields)**
```
# Numeric Range
GET /api/{entity}/getall?min{Field}={value}&max{Field}={value}

Examples:
- ?minAmount=50000&maxAmount=200000
- ?minPrice=10000&maxPrice=50000

# Date Range
GET /api/{entity}/getall?{field}From={date}&{field}To={date}

Examples:
- ?createdFrom=2025-11-01&createdTo=2025-11-30
- ?pickupFrom=2025-11-12T08:00:00&pickupTo=2025-11-12T18:00:00
```

### 5. **Boolean/Null Search**
```
# Boolean
GET /api/{entity}/getall?{field}=true|false

Examples:
- ?emailVerified=false
- ?deliverySuccess=true

# Null Check (for readAt, finishedAt, etc.)
GET /api/{entity}/getall?{field}Null=true|false
GET /api/{entity}/getall?unread=true  # readAt IS NULL
GET /api/{entity}/getall?read=true    # readAt IS NOT NULL

Examples:
- ?unread=true              # readAt IS NULL
- ?read=false               # readAt IS NULL
- ?finishedAtNull=true      # finishedAt IS NULL
```

### 6. **Combined Search + Sort + Pagination**
```
GET /api/{entity}/getall?
    {field1}={value1}&
    {field2}={value2}&
    min{Field3}={value3}&
    max{Field3}={value4}&
    sort={field},desc&
    page=0&
    size=10

Example:
GET /api/orders/getall?
    status=COMPLETED&
    userId=user-123&
    minAmount=100000&
    maxAmount=500000&
    createdFrom=2025-11-01&
    createdTo=2025-11-30&
    sort=createdAt,desc&
    page=0&
    size=20
```

---

## 🔍 Search Implementation Strategy

### Option 1: JPA Specification (Recommended) ⭐
**Pros:**
- Type-safe
- Reusable
- Easy to combine multiple criteria
- Support for complex queries

**Example:**
```java
public class OrderSpecification {
    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, query, cb) -> 
            status == null ? null : cb.equal(root.get("status"), status);
    }
    
    public static Specification<Order> hasUserId(String userId) {
        return (root, query, cb) -> 
            userId == null ? null : cb.equal(root.get("user").get("id"), userId);
    }
    
    public static Specification<Order> amountBetween(Double min, Double max) {
        return (root, query, cb) -> {
            if (min != null && max != null) {
                return cb.between(root.get("totalAmount"), min, max);
            } else if (min != null) {
                return cb.greaterThanOrEqualTo(root.get("totalAmount"), min);
            } else if (max != null) {
                return cb.lessThanOrEqualTo(root.get("totalAmount"), max);
            }
            return null;
        };
    }
}

// Usage in Service:
Specification<Order> spec = Specification.where(null);
if (status != null) spec = spec.and(OrderSpecification.hasStatus(status));
if (userId != null) spec = spec.and(OrderSpecification.hasUserId(userId));
if (minAmount != null || maxAmount != null) 
    spec = spec.and(OrderSpecification.amountBetween(minAmount, maxAmount));

List<Order> results = orderRepository.findAll(spec);
```

### Option 2: Query Methods (Simple cases)
```java
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByUserId(String userId);
    List<Order> findByStatusAndUserId(OrderStatus status, String userId);
}
```

### Option 3: JPQL Query (Complex cases)
```java
@Query("SELECT o FROM Order o WHERE " +
       "(:status IS NULL OR o.status = :status) AND " +
       "(:userId IS NULL OR o.user.id = :userId) AND " +
       "(:minAmount IS NULL OR o.totalAmount >= :minAmount) AND " +
       "(:maxAmount IS NULL OR o.totalAmount <= :maxAmount)")
List<Order> searchOrders(
    @Param("status") OrderStatus status,
    @Param("userId") String userId,
    @Param("minAmount") Double minAmount,
    @Param("maxAmount") Double maxAmount
);
```

**Recommendation:** Use **JPA Specification** for Phase 1 entities (Order, User, Promotion)

---

## 🛠️ Technical Implementation Steps

### Step 1: Create SearchRequest DTOs
```java
@Data
public class OrderSearchRequest {
    private String orderId;
    private String userId;
    private String storeId;
    private OrderStatus status;
    private List<OrderStatus> statuses; // Multi-select
    private Double minAmount;
    private Double maxAmount;
    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
    private LocalDateTime pickupFrom;
    private LocalDateTime pickupTo;
}
```

### Step 2: Create Specification Classes
```java
public class OrderSpecifications {
    public static Specification<Order> withSearchCriteria(OrderSearchRequest request) {
        return Specification
            .where(hasOrderId(request.getOrderId()))
            .and(hasUserId(request.getUserId()))
            .and(hasStoreId(request.getStoreId()))
            .and(hasStatus(request.getStatus()))
            .and(hasStatuses(request.getStatuses()))
            .and(amountBetween(request.getMinAmount(), request.getMaxAmount()))
            .and(createdBetween(request.getCreatedFrom(), request.getCreatedTo()))
            .and(pickupBetween(request.getPickupFrom(), request.getPickupTo()));
    }
    
    // Individual specification methods...
}
```

### Step 3: Update Repository
```java
public interface OrderRepository extends JpaRepository<Order, String>, 
                                        JpaSpecificationExecutor<Order> {
    // Existing methods...
}
```

### Step 4: Update Service
```java
public List<Order> searchOrders(OrderSearchRequest searchRequest) {
    Specification<Order> spec = OrderSpecifications.withSearchCriteria(searchRequest);
    return orderRepository.findAll(spec);
}
```

### Step 5: Update Controller
```java
@GetMapping("/getall")
public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> getAll(
        @ModelAttribute OrderSearchRequest searchRequest,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir) {
    
    List<Order> orders = orderService.searchOrders(searchRequest);
    List<Order> sortedOrders = sortOrders(orders, sortBy, sortDir);
    PagedResponse<OrderResponse> pagedResponse = createPagedResponse(sortedOrders, page, size);
    
    return ResponseEntity.ok(ApiResponse.success(200, "Orders retrieved successfully", pagedResponse));
}
```

---

## 📊 Search Fields Summary by Data Type

### String Fields (Partial Match, Case-Insensitive)
- `name`, `fullName`, `email`, `address`, `phone`, `code`, `title`, `description`
- Implementation: `LOWER(field) LIKE LOWER('%value%')`

### String Fields (Exact Match)
- `id`, `userId`, `orderId`, `storeId`, `ingredientId`, `categoryId`
- Implementation: `field = value`

### Enum Fields (Exact Match + Multi-Select)
- `status`, `role`, `type`, `kind`, `method`, `action`
- Implementation: `field = value` OR `field IN (value1, value2, value3)`

### Numeric Fields (Range)
- `totalAmount`, `linePrice`, `unitPrice`, `amount`, `balanceAfter`, `quantityChange`
- Implementation: `field BETWEEN min AND max` OR `field >= min` OR `field <= max`

### Date/DateTime Fields (Range)
- `createdAt`, `updatedAt`, `sentAt`, `capturedAt`, `pickupAt`, `startsAt`, `endsAt`
- Implementation: `field BETWEEN fromDate AND toDate`

### Boolean Fields
- `emailVerified`, `deliverySuccess`, `isActive`
- Implementation: `field = true/false`

### Nullable DateTime Fields (NULL Check)
- `readAt`, `finishedAt`, `handedAt`, `deletedAt`
- Implementation: `field IS NULL` OR `field IS NOT NULL`

---

## 💡 Best Practices

### 1. **Validation**
- Validate date ranges (from <= to)
- Validate numeric ranges (min <= max)
- Validate enum values
- Limit max page size (e.g., 100)

### 2. **Performance**
- Add database indexes on frequently searched fields:
  - `orders(status, user_id, store_id, created_at)`
  - `users(email, phone, role, status)`
  - `promotions(code, starts_at, ends_at)`
  - `ingredients(name, category_id)`

### 3. **Security**
- Only allow users to search their own data (except admins)
- Sanitize input to prevent SQL injection
- Rate limit search endpoints

### 4. **Documentation**
- Document all search parameters in Swagger/OpenAPI
- Provide examples for complex searches
- Document performance considerations

---

## 📝 Next Steps

1. ✅ **Phase 1:** Implement search for Order, User, Promotion
2. ✅ **Create Generic Search Utilities**
   - Generic Specification Builder
   - Generic SearchRequest Base Class
   - Common Validation Utils
3. ✅ **Update API Documentation**
   - Add search parameter descriptions to Swagger
   - Create search examples
4. ✅ **Performance Testing**
   - Test with large datasets
   - Optimize queries with indexes
5. ✅ **Phase 2-4:** Implement remaining entities

---

## 🔗 Related Documents

- **SORTING_CAPABILITY_ANALYSIS.md** - Sorting implementation guide
- **PAGINATION_IMPLEMENTATION_SUMMARY.md** - Pagination guide
- **backend_requirements.md** - Backend requirements

---

**Status:** ✅ Analysis Complete  
**Ready for Implementation:** YES  
**Recommended Start:** Phase 1 (Order, User, Promotion Search)  
**Estimated Timeline:** 8 weeks (4 phases × 2 weeks each)

---

## 📌 Quick Reference: Search Parameter Naming Convention

| Field Type | Parameter Name | Example |
|-----------|---------------|---------|
| Exact Match | `{field}` | `?status=PENDING` |
| Partial Match | `{field}` | `?name=nguyen` |
| Multi-Value | `{field}` (comma-separated) | `?status=PENDING,CONFIRMED` |
| Numeric Min | `min{Field}` | `?minAmount=50000` |
| Numeric Max | `max{Field}` | `?maxAmount=200000` |
| Date From | `{field}From` | `?createdFrom=2025-11-01` |
| Date To | `{field}To` | `?createdTo=2025-11-30` |
| Boolean | `{field}` | `?emailVerified=false` |
| Null Check | `{field}Null` or `unread/read` | `?unread=true` |

---

**End of Document**

