# 🔔 Hướng Dẫn Test Push Notification với Postman

## 📋 Yêu Cầu
- Backend đang chạy tại `http://localhost:8080`
- Đã import file `Push_Notification_Tests.postman_collection.json`
- Firebase đã được cấu hình

## ⚠️ LƯU Ý QUAN TRỌNG VỀ FCM TOKEN

**Token giả sẽ KHÔNG hoạt động!** Khi test với token giả như `test_fcm_token_abc123xyz`, bạn sẽ nhận được:
```json
{
    "status": 200,
    "data": {
        "success": 0,
        "failed": 1,
        "failedUserIds": ["user-id"]
    }
}
```

**✅ Cách test ĐÚNG:**
1. **Có app mobile/web thật:** Lấy FCM token thật từ Firebase SDK
2. **Test API quản lý notification:** Thay vì test gửi thật, test các API xem/đọc notification (xem phần Bước 5)
3. **Kiểm tra Database:** Xác nhận notification đã lưu vào DB (xem phần Troubleshooting)

**Hệ thống vẫn hoạt động đúng** nếu:
- ✅ Notification được lưu vào database
- ✅ API lấy danh sách notification hoạt động
- ✅ API đánh dấu đã đọc hoạt động
- ❌ Chỉ việc gửi qua Firebase bị fail vì token giả

## 🚀 Các Bước Test

### **Bước 1: Login để lấy Token**

#### 1.1. Login User
```
POST {{base_url}}/api/auth/login
Body:
{
  "email": "user@example.com",
  "password": "password123"
}
```

**✅ Kết quả mong đợi (200 OK):**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "email": "user@example.com",
    "role": "USER",
    "fullName": "Nguyen Van A"
  }
}
```
💾 **Lưu lại:** `user_token` và `user_id` từ response

#### 1.2. Login Admin
```
POST {{base_url}}/api/auth/login
Body:
{
  "email": "admin@example.com",
  "password": "admin123"
}
```

**✅ Kết quả mong đợi (200 OK):**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": "4fb95g75-6828-5673-c4gd-3d074g77bgb7",
    "email": "admin@example.com",
    "role": "ADMIN",
    "fullName": "Admin User"
  }
}
```
💾 **Lưu lại:** `admin_token` từ response

---

### **Bước 2: Đăng Ký FCM Token**

```
PUT {{base_url}}/api/users/{{user_id}}/fcm-token
Header: Authorization: Bearer {{user_token}}
Body:
{
  "fcmToken": "test_fcm_token_abc123xyz",
  "platform": "android",
  "deviceId": "test_device_001"
}
```

**✅ Kết quả mong đợi (200 OK):**
```json
{
  "success": true,
  "message": "FCM token saved successfully",
  "data": null
}
```

**❌ Nếu thất bại (400 Bad Request):**
```json
{
  "success": false,
  "message": "Failed to save FCM token: Invalid user ID",
  "data": null
}
```

**📱 Data Mẫu Khác:**
```json
// iOS Device
{
  "fcmToken": "test_ios_token_xyz789",
  "platform": "ios",
  "deviceId": "iphone_14_pro"
}

// Web Device
{
  "fcmToken": "test_web_token_web456",
  "platform": "web",
  "deviceId": "chrome_browser_001"
}
```

---

### **Bước 3: Test Gửi Notification Khuyến Mãi**

#### 3.1. Gửi cho 1 User
```
POST {{base_url}}/api/notifications/promotion
Header: Authorization: Bearer {{admin_token}}
Body:
{
  "userIds": ["{{user_id}}"],
  "title": "🎉 Chào mừng thành viên mới!",
  "message": "Giảm 20% đơn hàng đầu tiên. Mã: WELCOME20",
  "imageUrl": "https://example.com/welcome.jpg",
  "promoCode": "WELCOME20"
}
```

**✅ Kết quả mong đợi (200 OK):**
```json
{
  "success": true,
  "message": "Promotional notifications sent successfully",
  "data": {
    "sentCount": 1,
    "failedCount": 0,
    "totalUsers": 1
  }
}
```

**❌ Nếu user không có FCM token:**
```json
{
  "success": true,
  "message": "Promotional notifications sent successfully",
  "data": {
    "sentCount": 0,
    "failedCount": 1,
    "totalUsers": 1
  }
}
```

#### 3.2. Gửi cho nhiều User
```json
{
  "userIds": [
    "uuid-user-1",
    "uuid-user-2", 
    "uuid-user-3"
  ],
  "title": "🔥 Flash Sale 50% OFF!",
  "message": "Giảm 50% tất cả món ăn healthy. Chỉ hôm nay!",
  "imageUrl": "https://example.com/flash-sale.jpg",
  "promoCode": "FLASH50"
}
```

**✅ Kết quả mong đợi (200 OK):**
```json
{
  "success": true,
  "message": "Promotional notifications sent successfully",
  "data": {
    "sentCount": 3,
    "failedCount": 0,
    "totalUsers": 3
  }
}
```

**🎁 Các Data Mẫu Khác:**
```json
// Sinh nhật
{
  "userIds": ["user-id"],
  "title": "🎂 Sinh nhật vui vẻ!",
  "message": "Tặng bạn voucher 100K nhân dịp sinh nhật",
  "promoCode": "BIRTHDAY100"
}

// Khuyến mãi cuối tuần
{
  "userIds": ["user-id"],
  "title": "🌟 Weekend Special",
  "message": "Miễn phí giao hàng cho đơn từ 200K",
  "promoCode": "WEEKEND2024"
}

// Happy Hour
{
  "userIds": ["user-id"],
  "title": "⏰ Happy Hour 3PM-5PM",
  "message": "Giảm 30% tất cả món ăn trong khung giờ vàng",
  "promoCode": "HAPPY30"
}
```

---

### **Bước 4: Test Notification Tự Động (Order Status)**

Các notification này được tự động gửi khi cập nhật trạng thái đơn hàng:

#### 4.1. Order Confirmed
```
POST {{base_url}}/api/orders/{{order_id}}/status
Header: Authorization: Bearer {{admin_token}}
Body: {"status": "CONFIRMED"}
```
📩 **Notification:** "Đơn hàng đã được xác nhận"

**✅ Kết quả mong đợi (200 OK):**
```json
{
  "success": true,
  "message": "Order status updated successfully",
  "data": {
    "orderId": "order-uuid-123",
    "status": "CONFIRMED",
    "notificationSent": true
  }
}
```

#### 4.2. Order Preparing
```
Body: {"status": "PREPARING"}
```
📩 **Notification:** "Đầu bếp đang chuẩn bị món ăn"

**✅ Kết quả:** Status code 200, notification tự động gửi

#### 4.3. Order Ready
```
Body: {"status": "READY"}
```
📩 **Notification:** "Đơn hàng đã sẵn sàng để lấy!"

**✅ Kết quả:** Status code 200, notification tự động gửi

#### 4.4. Order Completed
```
Body: {"status": "COMPLETED"}
```
📩 **Notification:** "Chúc bạn ngon miệng!"

**✅ Kết quả:** Status code 200, notification tự động gửi

#### 4.5. Order Cancelled
```
Body: {"status": "CANCELLED"}
```
📩 **Notification:** "Đơn hàng đã bị hủy"

**✅ Kết quả:** Status code 200, notification tự động gửi

---

### **Bước 5: Kiểm Tra Lịch Sử Notification**

#### 5.1. Lấy tất cả notification (phân trang)
```
GET {{base_url}}/api/users/{{user_id}}/notifications?page=0&size=20
Header: Authorization: Bearer {{user_token}}
```

**✅ Kết quả mong đợi (200 OK):**
```json
{
  "content": [
    {
      "id": "notif-uuid-1",
      "userId": "user-uuid",
      "title": "🎉 Chào mừng thành viên mới!",
      "message": "Giảm 20% đơn hàng đầu tiên. Mã: WELCOME20",
      "type": "PROMOTIONAL",
      "imageUrl": "https://example.com/welcome.jpg",
      "isRead": false,
      "createdAt": "2025-10-30T10:30:00",
      "data": {
        "promoCode": "WELCOME20"
      }
    },
    {
      "id": "notif-uuid-2",
      "userId": "user-uuid",
      "title": "Đơn hàng đã được xác nhận",
      "message": "Đơn hàng #12345 đang được xử lý",
      "type": "ORDER_UPDATE",
      "isRead": true,
      "createdAt": "2025-10-30T09:15:00",
      "data": {
        "orderId": "order-uuid-123",
        "orderStatus": "CONFIRMED"
      }
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 2,
  "totalPages": 1,
  "last": true
}
```

#### 5.2. Lấy notification chưa đọc
```
GET {{base_url}}/api/users/{{user_id}}/notifications/unread
Header: Authorization: Bearer {{user_token}}
```

**✅ Kết quả mong đợi (200 OK):**
```json
[
  {
    "id": "notif-uuid-1",
    "userId": "user-uuid",
    "title": "🎉 Chào mừng thành viên mới!",
    "message": "Giảm 20% đơn hàng đầu tiên. Mã: WELCOME20",
    "type": "PROMOTIONAL",
    "isRead": false,
    "createdAt": "2025-10-30T10:30:00",
    "data": {
      "promoCode": "WELCOME20"
    }
  }
]
```

#### 5.3. Đếm số notification chưa đọc
```
GET {{base_url}}/api/users/{{user_id}}/notifications/unread-count
Header: Authorization: Bearer {{user_token}}
```

**✅ Kết quả mong đợi (200 OK):**
```json
{
  "unreadCount": 5
}
```

**Giải thích:** 
- `unreadCount: 0` → Không có notification chưa đọc ✅
- `unreadCount: 5` → Có 5 notification chưa đọc 🔔

#### 5.4. Đánh dấu 1 notification đã đọc
```
PUT {{base_url}}/api/notifications/{{notification_id}}/read
Header: Authorization: Bearer {{user_token}}
```

**✅ Kết quả mong đợi (200 OK):**
```json
{
  "success": true,
  "message": "Notification marked as read",
  "data": null
}
```

**❌ Nếu notification không tồn tại (404):**
```json
{
  "success": false,
  "message": "Notification not found",
  "data": null
}
```

#### 5.5. Đánh dấu TẤT CẢ đã đọc
```
PUT {{base_url}}/api/users/{{user_id}}/notifications/read-all
Header: Authorization: Bearer {{user_token}}
```

**✅ Kết quả mong đợi (200 OK):**
```json
{
  "success": true,
  "message": "All notifications marked as read",
  "data": {
    "updatedCount": 5
  }
}
```

---

### **Bước 6: Logout (Xóa FCM Token)**

```
DELETE {{base_url}}/api/users/{{user_id}}/fcm-token
Header: Authorization: Bearer {{user_token}}
```

**✅ Kết quả mong đợi (200 OK):**
```json
{
  "success": true,
  "message": "FCM token removed successfully",
  "data": null
}
```

**Lưu ý:** User sẽ không nhận notification nữa sau khi logout ❌🔔

---

## 📊 Kịch Bản Test Hoàn Chỉnh

### **Scenario 1: User nhận notification đơn hàng**
1. Login User → Lấy token
2. Đăng ký FCM Token
3. Tạo đơn hàng mới
4. Admin cập nhật status: CONFIRMED → User nhận notification
5. Admin cập nhật status: PREPARING → User nhận notification
6. Admin cập nhật status: READY → User nhận notification
7. User kiểm tra danh sách notification
8. User đánh dấu đã đọc

### **Scenario 2: Admin gửi khuyến mãi hàng loạt**
1. Login Admin → Lấy admin token
2. Chuẩn bị danh sách userIds
3. Gửi promotional notification
4. User login và kiểm tra notification
5. User đọc notification và sử dụng mã khuyến mãi

### **Scenario 3: Test unread count badge**
1. Admin gửi 3 promotional notifications
2. Admin trigger 2 order status changes
3. User gọi API unread-count → Kết quả: 5
4. User đọc 2 notification
5. User gọi lại API unread-count → Kết quả: 3

---

## 🔧 Troubleshooting

### ⚠️ Status 200 nhưng failed = 1, success = 0

**🔍 Tại sao status 200 mà vẫn fail?**

API trả về status 200 OK vì:
- ✅ Request hợp lệ
- ✅ User tồn tại
- ✅ FCM token đã đăng ký trong DB
- ✅ Notification đã được lưu vào DB

**NHƯNG gửi qua Firebase thất bại vì:**
- ❌ Token giả `test_fcm_token_abc123xyz` không tồn tại trong Firebase
- ❌ Firebase trả về lỗi `INVALID_ARGUMENT` hoặc `UNREGISTERED`

**Response lỗi:**
```json
{
    "status": 200,
    "message": "Promotional notifications sent",
    "data": {
        "total": 1,
        "failedUserIds": ["02a9a862-7fd7-4295-bebb-2782b3e56691"],
        "success": 0,
        "failed": 1
    }
}
```

**💡 Đây KHÔNG PHẢI là lỗi code!** Hệ thống hoạt động đúng, chỉ là token giả không gửi được qua Firebase.

**✅ Giải pháp:**

#### **Option 1: Lấy FCM Token thật từ Firebase (Production)**
Dùng Firebase SDK trên app mobile/web để lấy token thật:

**Android (Kotlin):**
```kotlin
FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
    if (task.isSuccessful) {
        val token = task.result
        println("FCM Token: $token")
    }
}
```

**iOS (Swift):**
```swift
Messaging.messaging().token { token, error in
    if let token = token {
        print("FCM Token: \(token)")
    }
}
```

**Web (JavaScript):**
```javascript
import { getToken } from "firebase/messaging";
const token = await getToken(messaging, { vapidKey: 'YOUR_VAPID_KEY' });
console.log('FCM Token:', token);
```

Sau khi có token thật, test lại sẽ thành công.

#### **Option 2: Kiểm tra Database (Testing)**
Kiểm tra notification đã được lưu vào DB:

```sql
SELECT * FROM notifications 
WHERE user_id = '02a9a862-7fd7-4295-bebb-2782b3e56691' 
ORDER BY sent_at DESC;
```

✅ Nếu có bản ghi với `delivery_success = false` → Hệ thống hoạt động đúng!

#### **Option 3: Test API quản lý notification thay vì gửi thật**
1. Đăng ký token giả (để có data trong DB)
2. Gửi promotional notification (sẽ fail nhưng vẫn lưu DB)
3. **Test các API này:**
   ```
   GET {{base_url}}/api/users/{{user_id}}/notifications
   GET {{base_url}}/api/users/{{user_id}}/notifications/unread-count
   PUT {{base_url}}/api/notifications/{{notification_id}}/read
   ```
4. ✅ Kiểm tra notification có xuất hiện trong danh sách

### ❌ Không nhận được notification
- **Token giả:** Firebase từ chối token không hợp lệ
- **Firebase chưa config:** Kiểm tra file `firebase-service-account.json`
- **User không có FCM token:** Phải đăng ký FCM token trước
- **Kiểm tra logs backend:** Tìm dòng "❌ Failed to send notification"

### ❌ 401 Unauthorized
- Kiểm tra token còn hạn không
- Login lại để lấy token mới

### ❌ 403 Forbidden
- Kiểm tra role của user (USER/ADMIN)
- Đảm bảo dùng đúng token cho từng endpoint

---

## 📝 Notes

- **FCM Token:** Trong test, dùng token giả. Trong production, lấy từ Firebase SDK
- **Order ID:** Cần có order thực trong DB trước khi test order notifications
- **User IDs:** Phải là UUID hợp lệ trong database
- **Image URLs:** Optional, có thể bỏ qua trong test

---

## ✅ Checklist Test

- [ ] Login user và admin thành công
- [ ] Đăng ký FCM token thành công
- [ ] Gửi promotional notification cho 1 user
- [ ] Gửi promotional notification cho nhiều user
- [ ] Test 5 trạng thái order notification
- [ ] Lấy danh sách notification với phân trang
- [ ] Lấy danh sách notification chưa đọc
- [ ] Đếm số notification chưa đọc
- [ ] Đánh dấu 1 notification đã đọc
- [ ] Đánh dấu tất cả đã đọc
- [ ] Xóa FCM token khi logout

**🎉 Hoàn thành! Hệ thống Push Notification hoạt động tốt!**

