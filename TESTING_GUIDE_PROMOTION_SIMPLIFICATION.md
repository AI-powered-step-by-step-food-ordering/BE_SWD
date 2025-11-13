# Hướng dẫn Test API Promotion sau khi đơn giản hóa

## Prerequisites
- Server đang chạy tại `http://localhost:8080`
- Có Bearer token để authenticate
- Đã chạy migration SQL để update database schema

## 1. Test Create Promotion

### Request
```bash
POST http://localhost:8080/api/promotions/create
Content-Type: application/json
Authorization: Bearer YOUR_TOKEN_HERE

{
  "code": "SUMMER20",
  "name": "Giảm giá mùa hè 20%",
  "discountPercent": 20,
  "startsAt": "2025-11-13T00:00:00Z",
  "endsAt": "2025-12-31T23:59:59Z",
  "isActive": true,
  "imageUrl": "https://example.com/summer.jpg"
}
```

### Expected Response
```json
{
  "statusCode": 201,
  "message": "Promotion created successfully",
  "data": {
    "id": "generated-uuid",
    "code": "SUMMER20",
    "name": "Giảm giá mùa hè 20%",
    "discountPercent": 20.0,
    "minOrderValue": 50000.0,
    "active": true,
    "imageUrl": "https://example.com/summer.jpg",
    "createdAt": "2025-11-13T...",
    "startsAt": "2025-11-13T00:00:00Z",
    "endsAt": "2025-12-31T23:59:59Z"
  }
}
```

## 2. Test Get All Promotions

### Request
```bash
GET http://localhost:8080/api/promotions/getall?page=0&size=10
Authorization: Bearer YOUR_TOKEN_HERE
```

### Expected Response
```json
{
  "statusCode": 200,
  "message": "Promotions retrieved successfully",
  "data": {
    "content": [
      {
        "id": "uuid",
        "code": "SUMMER20",
        "name": "Giảm giá mùa hè 20%",
        "discountPercent": 20.0,
        "minOrderValue": 50000.0,
        "active": true,
        "imageUrl": "...",
        "createdAt": "..."
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

## 3. Test Search Promotions

### Test 3a: Search by status = active
```bash
GET http://localhost:8080/api/promotions/search?status=active
Authorization: Bearer YOUR_TOKEN_HERE
```

### Test 3b: Search by name
```bash
GET http://localhost:8080/api/promotions/search?name=mùa hè
Authorization: Bearer YOUR_TOKEN_HERE
```

### Test 3c: Search by code
```bash
GET http://localhost:8080/api/promotions/search?code=SUMMER20
Authorization: Bearer YOUR_TOKEN_HERE
```

## 4. Test Create Order (để test apply promotion)

### Request
```bash
POST http://localhost:8080/api/orders/create
Content-Type: application/json
Authorization: Bearer YOUR_TOKEN_HERE

{
  "storeId": "your-store-id",
  "userId": "your-user-id",
  "note": "Test order for promotion"
}
```

Lưu lại `orderId` từ response để test tiếp.

## 5. Test Add Bowls to Order

Thêm bowls vào order để có subtotal.

```bash
POST http://localhost:8080/api/bowls/create
Content-Type: application/json
Authorization: Bearer YOUR_TOKEN_HERE

{
  "orderId": "order-id-from-step-4",
  "templateId": "some-template-id",
  "name": "My Bowl"
}
```

Sau đó add items vào bowl để có giá trị > 50000.

## 6. Test Apply Promotion (QUAN TRỌNG!)

### Test 6a: Apply promotion thành công
```bash
POST http://localhost:8080/api/orders/apply-promo/ORDER_ID?code=SUMMER20
Authorization: Bearer YOUR_TOKEN_HERE
```

### Expected Response
```json
{
  "statusCode": 200,
  "message": "Promotion applied successfully",
  "data": {
    "id": "order-id",
    "subtotalAmount": 100000.0,
    "promotionTotal": 20000.0,    // = 100000 × 20%
    "totalAmount": 80000.0,        // = 100000 - 20000
    "status": "PENDING",
    ...
  }
}
```

### Test 6b: Apply cùng promotion 2 lần (should fail)
```bash
# Apply lần 2
POST http://localhost:8080/api/orders/apply-promo/ORDER_ID?code=SUMMER20
Authorization: Bearer YOUR_TOKEN_HERE
```

### Expected Error Response
```json
{
  "statusCode": 400,
  "errorCode": "BAD_REQUEST",
  "message": "Promotion already applied to this order"
}
```

### Test 6c: Apply promotion đã expired
```bash
# Tạo promotion với endsAt trong quá khứ
POST http://localhost:8080/api/promotions/create
{
  "code": "EXPIRED",
  "name": "Expired promotion",
  "discountPercent": 10,
  "startsAt": "2025-01-01T00:00:00Z",
  "endsAt": "2025-01-31T23:59:59Z",  // Đã hết hạn
  "isActive": true
}

# Sau đó apply
POST http://localhost:8080/api/orders/apply-promo/ORDER_ID?code=EXPIRED
```

### Expected Error Response
```json
{
  "statusCode": 400,
  "errorCode": "BAD_REQUEST",
  "message": "Promotion has expired"
}
```

### Test 6d: Apply promotion chưa bắt đầu
```bash
# Tạo promotion với startsAt trong tương lai
POST http://localhost:8080/api/promotions/create
{
  "code": "FUTURE",
  "name": "Future promotion",
  "discountPercent": 10,
  "startsAt": "2026-01-01T00:00:00Z",  // Chưa bắt đầu
  "endsAt": "2026-12-31T23:59:59Z",
  "isActive": true
}

# Sau đó apply
POST http://localhost:8080/api/orders/apply-promo/ORDER_ID?code=FUTURE
```

### Expected Error Response
```json
{
  "statusCode": 400,
  "errorCode": "BAD_REQUEST",
  "message": "Promotion has not started yet"
}
```

### Test 6e: Apply promotion không tồn tại
```bash
POST http://localhost:8080/api/orders/apply-promo/ORDER_ID?code=NOTEXIST
```

### Expected Error Response
```json
{
  "statusCode": 404,
  "errorCode": "NOT_FOUND",
  "message": "Promotion not found or inactive: NOTEXIST"
}
```

## 7. Test Update Promotion

```bash
PUT http://localhost:8080/api/promotions/update/PROMOTION_ID
Content-Type: application/json
Authorization: Bearer YOUR_TOKEN_HERE

{
  "code": "SUMMER25",
  "name": "Giảm giá mùa hè 25% (Updated)",
  "discountPercent": 25,
  "startsAt": "2025-11-13T00:00:00Z",
  "endsAt": "2025-12-31T23:59:59Z",
  "isActive": true,
  "imageUrl": "https://example.com/summer-new.jpg"
}
```

## 8. Verify PromotionRedemption được tạo

```bash
GET http://localhost:8080/api/promotion_redemptions/getall
Authorization: Bearer YOUR_TOKEN_HERE
```

### Expected Response
```json
{
  "statusCode": 200,
  "message": "Promotion redemptions retrieved successfully",
  "data": {
    "content": [
      {
        "id": "redemption-id",
        "promotionId": "promotion-id",
        "orderId": "order-id",
        "status": "APPLIED",
        "createdAt": "..."
      }
    ],
    ...
  }
}
```

## 9. So sánh: apply-promo vs PromotionRedemption API

### ❌ KHÔNG NÊN dùng (Manual, không an toàn)
```bash
POST http://localhost:8080/api/promotion_redemptions/create
{
  "promotionId": "promo-id",
  "orderId": "order-id",
  "status": "APPLIED"
}
```

**Vấn đề:**
- Không validate promotion (expired, min order value, etc.)
- Không auto recalc order totals
- Có thể tạo dữ liệu sai

### ✅ NÊN DÙNG (Recommended)
```bash
POST http://localhost:8080/api/orders/apply-promo/ORDER_ID?code=SUMMER20
```

**Lợi ích:**
- Validate đầy đủ business rules
- Auto tạo PromotionRedemption
- Auto recalc order totals
- An toàn và đúng logic

## 10. Test Edge Cases

### Test 10a: Discount 100%
```bash
POST http://localhost:8080/api/promotions/create
{
  "code": "FREE100",
  "name": "Miễn phí 100%",
  "discountPercent": 100,
  "isActive": true
}

# Apply vào order
POST http://localhost:8080/api/orders/apply-promo/ORDER_ID?code=FREE100
```

Expected: totalAmount = 0

### Test 10b: Discount 0%
```bash
POST http://localhost:8080/api/promotions/create
{
  "code": "ZERO",
  "name": "Không giảm giá",
  "discountPercent": 0,
  "isActive": true
}
```

Expected: totalAmount = subtotalAmount

### Test 10c: Invalid discount (> 100)
```bash
POST http://localhost:8080/api/promotions/create
{
  "code": "INVALID",
  "name": "Invalid",
  "discountPercent": 150,  // > 100
  "isActive": true
}
```

Expected: Validation error

### Test 10d: Invalid discount (< 0)
```bash
POST http://localhost:8080/api/promotions/create
{
  "code": "INVALID2",
  "name": "Invalid",
  "discountPercent": -10,  // < 0
  "isActive": true
}
```

Expected: Validation error

## Checklist

- [ ] Create promotion thành công
- [ ] Get all promotions
- [ ] Search promotion by status (active/expired/upcoming)
- [ ] Search promotion by name
- [ ] Apply promotion thành công với order hợp lệ
- [ ] Apply promotion fail khi đã apply trước đó
- [ ] Apply promotion fail khi expired
- [ ] Apply promotion fail khi chưa bắt đầu
- [ ] Apply promotion fail khi code không tồn tại
- [ ] Order totals được tính đúng (discount = subtotal × percent / 100)
- [ ] PromotionRedemption được tạo tự động
- [ ] Update promotion thành công
- [ ] Validate discountPercent (0-100)
- [ ] Test edge cases (0%, 100%, invalid values)

---

**Lưu ý:**
- Thay `YOUR_TOKEN_HERE` bằng JWT token thực
- Thay các ID (orderId, promotionId, etc.) bằng ID thực từ database
- Đảm bảo đã chạy migration SQL trước khi test


