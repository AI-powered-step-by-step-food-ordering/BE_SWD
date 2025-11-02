# Sorting Capability Analysis - Entities & Controllers

## Date: November 2, 2025

Phân tích chi tiết các entity và controller để xác định khả năng tích hợp tính năng **sorting** vào các endpoint GET.

---

## 📊 Summary

| Entity | Sorting Priority | Recommended Sort Fields | Use Case |
|--------|-----------------|------------------------|----------|
| **Order** | 🔥 **HIGH** | createdAt, totalAmount, status, pickupAt | Admin dashboard, User history |
| **PaymentTransaction** | 🔥 **HIGH** | createdAt, amount, status, capturedAt | Transaction history, Reports |
| **Promotion** | 🔥 **HIGH** | createdAt, startsAt, endsAt, code, name | Promotion management |
| **User** | 🔴 **MEDIUM-HIGH** | createdAt, fullName, email, role | User management |
| **Ingredient** | 🔴 **MEDIUM-HIGH** | name, unitPrice, category | Menu builder, Inventory |
| **Bowl** | 🟡 **MEDIUM** | name, linePrice, createdAt | Order details |
| **BowlTemplate** | 🟡 **MEDIUM** | name, createdAt | Template selection |
| **Store** | 🟡 **MEDIUM** | name, createdAt | Store management |
| **Category** | 🟡 **MEDIUM** | name, kind | Category browsing |
| **KitchenJob** | 🔥 **HIGH** | createdAt, status, startedAt, finishedAt | Kitchen workflow |
| **Inventory** | 🔴 **MEDIUM-HIGH** | createdAt, action, balanceAfter | Stock management |
| **PromotionRedemption** | 🟡 **MEDIUM** | createdAt, status | Analytics |
| **Notification** | 🔥 **HIGH** | sentAt, readAt, type | User inbox |

---

## 🔥 HIGH PRIORITY Entities (Cần Sort Ngay)

### 1. **Order** 📦
**Controller:** `OrderController.java`  
**Endpoints:**
- `GET /api/orders/getall`
- `GET /api/orders/order-history/{userId}`

**Entity Fields:**
```java
- id: String
- pickupAt: OffsetDateTime ⭐ SORTABLE
- status: OrderStatus (PENDING, CONFIRMED, PREPARING, READY, COMPLETED, CANCELLED) ⭐ SORTABLE
- subtotalAmount: Double ⭐ SORTABLE
- promotionTotal: Double ⭐ SORTABLE
- totalAmount: Double ⭐ SORTABLE
- createdAt: ZonedDateTime ⭐ SORTABLE (from BaseEntity)
- updatedAt: ZonedDateTime ⭐ SORTABLE (from BaseEntity)
```

**Recommended Sort Fields:**
```
?sort=createdAt,desc          // Mới nhất trước (default)
?sort=totalAmount,desc        // Giá cao nhất
?sort=totalAmount,asc         // Giá thấp nhất
?sort=status,asc              // Sort theo trạng thái
?sort=pickupAt,asc            // Sắp xếp theo thời gian pickup
```

**Use Cases:**
- ✅ Admin xem orders mới nhất
- ✅ User xem lịch sử đơn hàng theo thời gian
- ✅ Filter orders theo status + sort theo giá
- ✅ Sort theo pickup time để quản lý lấy hàng

---

### 2. **PaymentTransaction** 💳
**Controller:** `PaymentTransactionController.java`  
**Endpoints:**
- `GET /api/payment_transactions/getall`
- `GET /api/payment_transactions/payment-history/{userId}`

**Entity Fields:**
```java
- id: String
- method: PaymentMethod (CASH, ZALOPAY, etc.) ⭐ SORTABLE
- status: PaymentStatus (PENDING, SUCCESS, FAILED) ⭐ SORTABLE
- amount: Double ⭐ SORTABLE
- capturedAt: OffsetDateTime ⭐ SORTABLE
- createdAt: ZonedDateTime ⭐ SORTABLE
```

**Recommended Sort Fields:**
```
?sort=createdAt,desc          // Mới nhất trước (default)
?sort=capturedAt,desc         // Theo thời gian thanh toán
?sort=amount,desc             // Số tiền cao nhất
?sort=status,asc              // Theo trạng thái
```

**Use Cases:**
- ✅ Admin xem transactions mới nhất
- ✅ User xem lịch sử thanh toán
- ✅ Report theo thời gian
- ✅ Sort theo số tiền để phân tích

---

### 3. **KitchenJob** 👨‍🍳
**Controller:** `KitchenJobController.java`  
**Endpoint:** `GET /api/kitchen_jobs/getall`

**Entity Fields:**
```java
- id: String
- status: JobStatus (QUEUED, IN_PROGRESS, COMPLETED, CANCELLED) ⭐ SORTABLE
- startedAt: OffsetDateTime ⭐ SORTABLE
- finishedAt: OffsetDateTime ⭐ SORTABLE
- handedAt: OffsetDateTime ⭐ SORTABLE
- createdAt: ZonedDateTime ⭐ SORTABLE
```

**Recommended Sort Fields:**
```
?sort=createdAt,asc           // Older jobs first (FIFO queue)
?sort=status,asc              // Theo priority status
?sort=startedAt,asc           // Theo thời gian bắt đầu
?sort=finishedAt,desc         // Jobs hoàn thành gần nhất
```

**Use Cases:**
- ✅ Kitchen staff xem jobs theo thứ tự FIFO
- ✅ Manager track completion time
- ✅ Dashboard hiển thị jobs đang IN_PROGRESS

---

### 4. **Notification** 🔔
**Controller:** `NotificationController.java`  
**Endpoints:** (Need to check if exist)

**Entity Fields:**
```java
- id: String
- title: String
- type: NotificationType ⭐ SORTABLE
- orderStatus: OrderStatus ⭐ SORTABLE
- sentAt: OffsetDateTime ⭐ SORTABLE (NOT NULL)
- readAt: OffsetDateTime ⭐ SORTABLE
- deliverySuccess: Boolean ⭐ SORTABLE
- createdAt: Not exist (need to add or use sentAt)
```

**Recommended Sort Fields:**
```
?sort=sentAt,desc             // Mới nhất trước (default)
?sort=readAt,asc              // Unread first (null first)
?sort=type,asc                // Group by type
```

**Use Cases:**
- ✅ User xem notifications mới nhất
- ✅ Show unread notifications first
- ✅ Filter by type + sort by time

---

### 5. **Promotion** 🎁
**Controller:** `PromotionController.java` (extends BaseController)  
**Endpoints:**
- `GET /api/promotions/getall`
- `GET /api/promotions/active`

**Entity Fields:**
```java
- id: String
- code: String ⭐ SORTABLE
- name: String ⭐ SORTABLE
- type: PromotionType ⭐ SORTABLE
- percentOff: BigDecimal ⭐ SORTABLE
- amountOff: BigDecimal ⭐ SORTABLE
- minOrderValue: BigDecimal ⭐ SORTABLE
- startsAt: OffsetDateTime ⭐ SORTABLE
- endsAt: OffsetDateTime ⭐ SORTABLE
- maxRedemptions: Integer ⭐ SORTABLE
- createdAt: ZonedDateTime ⭐ SORTABLE
```

**Recommended Sort Fields:**
```
?sort=startsAt,desc           // Newest promotions
?sort=endsAt,asc              // Expiring soon
?sort=percentOff,desc         // Highest discount
?sort=amountOff,desc          // Biggest amount off
?sort=code,asc                // Alphabetical
```

**Use Cases:**
- ✅ Admin xem promotions mới nhất
- ✅ User xem promotions expiring soon
- ✅ Sort by discount value
- ✅ Alphabetical for easy finding

---

## 🔴 MEDIUM-HIGH PRIORITY Entities

### 6. **User** 👤
**Controller:** `UserController.java` (extends BaseController)  
**Endpoints:**
- `GET /api/users/getall`
- `GET /api/users/active`
- `GET /api/users/inactive`

**Entity Fields:**
```java
- id: String
- fullName: String ⭐ SORTABLE
- email: String ⭐ SORTABLE
- role: Role (USER, STAFF, MANAGER, ADMIN) ⭐ SORTABLE
- status: AccountStatus (ACTIVE, INACTIVE, SUSPENDED) ⭐ SORTABLE
- dateOfBirth: LocalDate ⭐ SORTABLE
- emailVerified: Boolean ⭐ SORTABLE
- createdAt: ZonedDateTime ⭐ SORTABLE
```

**Recommended Sort Fields:**
```
?sort=createdAt,desc          // Newest users
?sort=fullName,asc            // Alphabetical
?sort=email,asc               // Alphabetical by email
?sort=role,asc                // Group by role
```

**Use Cases:**
- ✅ Admin user management
- ✅ Search users by name
- ✅ Filter by role + sort

---

### 7. **Ingredient** 🥬
**Controller:** `IngredientController.java` (extends BaseController)  
**Endpoints:**
- `GET /api/ingredients/getall`
- `GET /api/ingredients/active`

**Entity Fields:**
```java
- id: String
- name: String ⭐ SORTABLE
- unit: String
- standardQuantity: Double
- unitPrice: Double ⭐ SORTABLE
- createdAt: ZonedDateTime ⭐ SORTABLE
```

**Recommended Sort Fields:**
```
?sort=name,asc                // Alphabetical (most common)
?sort=unitPrice,asc           // Cheapest first
?sort=unitPrice,desc          // Most expensive
?sort=createdAt,desc          // Newest ingredients
```

**Use Cases:**
- ✅ Menu builder: browse ingredients alphabetically
- ✅ Budget planning: sort by price
- ✅ Inventory: find items by name

---

### 8. **Inventory** 📊
**Controller:** `InventoryController.java`  
**Endpoint:** `GET /api/inventories/getall`

**Entity Fields:**
```java
- id: String
- action: StockAction (IN, OUT, ADJUST) ⭐ SORTABLE
- quantityChange: Double ⭐ SORTABLE
- balanceAfter: Double ⭐ SORTABLE
- createdAt: ZonedDateTime ⭐ SORTABLE
```

**Recommended Sort Fields:**
```
?sort=createdAt,desc          // Latest transactions
?sort=balanceAfter,asc        // Low stock first
?sort=action,asc              // Group by action type
```

**Use Cases:**
- ✅ Stock management
- ✅ Audit trail by time
- ✅ Low stock alerts

---

## 🟡 MEDIUM PRIORITY Entities

### 9. **Bowl** 🥗
**Controller:** `BowlController.java`  
**Endpoint:** `GET /api/bowls/getall`

**Entity Fields:**
```java
- id: String
- name: String ⭐ SORTABLE
- linePrice: Double ⭐ SORTABLE
- createdAt: ZonedDateTime ⭐ SORTABLE
```

**Recommended Sort Fields:**
```
?sort=name,asc
?sort=linePrice,desc
?sort=createdAt,desc
```

---

### 10. **BowlTemplate** 📋
**Controller:** `BowlTemplateController.java` (extends BaseController)  
**Endpoints:**
- `GET /api/bowl_templates/getall`
- `GET /api/bowl_templates/active`

**Entity Fields:**
```java
- id: String
- name: String ⭐ SORTABLE
- createdAt: ZonedDateTime ⭐ SORTABLE
```

**Recommended Sort Fields:**
```
?sort=name,asc                // Alphabetical
?sort=createdAt,desc          // Newest templates
```

**Use Cases:**
- ✅ Template selection UI
- ✅ Admin template management

---

### 11. **Store** 🏪
**Controller:** `StoreController.java` (extends BaseController)  
**Endpoints:**
- `GET /api/stores/getall`
- `GET /api/stores/active`

**Entity Fields:**
```java
- id: String
- name: String ⭐ SORTABLE
- address: String ⭐ SORTABLE
- createdAt: ZonedDateTime ⭐ SORTABLE
```

**Recommended Sort Fields:**
```
?sort=name,asc                // Alphabetical
?sort=createdAt,desc          // Newest stores
```

---

### 12. **Category** 📂
**Controller:** `CategoryController.java` (extends BaseController)  
**Endpoints:**
- `GET /api/categories/getall`
- `GET /api/categories/active`

**Entity Fields:**
```java
- id: String
- name: String ⭐ SORTABLE
- kind: IngredientKind (BASE, PROTEIN, VEGGIE, TOPPING, SAUCE) ⭐ SORTABLE
- createdAt: ZonedDateTime ⭐ SORTABLE
```

**Recommended Sort Fields:**
```
?sort=name,asc                // Alphabetical
?sort=kind,asc                // Group by type
```

---

### 13. **PromotionRedemption** 🎫
**Controller:** `PromotionRedemptionController.java`  
**Endpoint:** `GET /api/promotion_redemptions/getall`

**Entity Fields:**
```java
- id: String
- status: RedemptionStatus (APPLIED, CANCELLED) ⭐ SORTABLE
- createdAt: ZonedDateTime ⭐ SORTABLE
```

**Recommended Sort Fields:**
```
?sort=createdAt,desc          // Latest redemptions
?sort=status,asc              // Group by status
```

---

## 📋 Implementation Recommendations

### Phase 1: HIGH PRIORITY (Implement First)
1. ✅ **Order** - Most critical for users & admin
2. ✅ **PaymentTransaction** - Important for finance
3. ✅ **KitchenJob** - Critical for operations
4. ✅ **Notification** - Important for UX
5. ✅ **Promotion** - Important for marketing

### Phase 2: MEDIUM-HIGH PRIORITY
6. ✅ **User** - Admin tools
7. ✅ **Ingredient** - Menu building
8. ✅ **Inventory** - Stock management

### Phase 3: MEDIUM PRIORITY (Nice to Have)
9. ✅ **Bowl**, **BowlTemplate**, **Store**, **Category**, **PromotionRedemption**

---

## 🎯 Recommended Sort Parameter Format

```
GET /api/{entity}/getall?page=0&size=10&sort={field},{direction}

Examples:
- ?sort=createdAt,desc
- ?sort=name,asc
- ?sort=totalAmount,desc
- ?sort=status,asc&sort=createdAt,desc  // Multiple sorts
```

### Sort Directions:
- `asc` - Ascending (A-Z, 0-9, oldest-newest)
- `desc` - Descending (Z-A, 9-0, newest-oldest)

### Default Behavior:
- If no `sort` parameter: **sort by `createdAt,desc`** (newest first)
- For alphabetical entities (names): default to **`name,asc`**

---

## 🔍 Sort Fields by Data Type

### String Fields (Alphabetical)
- `name`, `email`, `code`, `title`, `address`, `phone`
- Sort: A-Z (asc) or Z-A (desc)

### Numeric Fields
- `totalAmount`, `linePrice`, `unitPrice`, `amount`, `balanceAfter`, `quantityChange`
- Sort: Low-High (asc) or High-Low (desc)

### DateTime Fields
- `createdAt`, `updatedAt`, `sentAt`, `readAt`, `capturedAt`, `pickupAt`, `startsAt`, `endsAt`, `startedAt`, `finishedAt`
- Sort: Oldest-Newest (asc) or Newest-Oldest (desc)

### Enum Fields
- `status`, `role`, `type`, `kind`, `method`, `action`
- Sort: Alphabetical by enum name (asc/desc)

### Boolean Fields
- `emailVerified`, `deliverySuccess`, `isActive`
- Sort: false first (asc) or true first (desc)

---

## 💡 Next Steps

1. **Update BaseController** to add sorting support
2. **Create SortRequest DTO** for handling sort parameters
3. **Update service layer** to handle sorting logic
4. **Test sorting** with all recommended fields
5. **Update Swagger/OpenAPI docs** with sort examples
6. **Update PAGINATION_IMPLEMENTATION_SUMMARY.md** to include sorting

---

## 📝 Notes

- All entities extending `BaseEntity` have: `createdAt`, `updatedAt`, `deletedAt`, `isActive`
- Most common sort: **`createdAt,desc`** (newest first)
- For user-facing lists: **`name,asc`** (alphabetical)
- For transaction lists: **`createdAt,desc`** or specific time field
- For status-based entities: **`status,asc`** + **`createdAt,desc`**

---

**Status:** ✅ Analysis Complete  
**Ready for Implementation:** YES  
**Recommended Start:** Phase 1 (HIGH PRIORITY entities)

