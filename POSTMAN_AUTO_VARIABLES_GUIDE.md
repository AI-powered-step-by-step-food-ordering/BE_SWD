# 🚀 Hướng Dẫn Tự Động Lưu Biến Môi Trường trong Postman

## ❓ Vấn Đề
Khi test API, bạn thường phải:
1. ❌ Copy token từ response Login
2. ❌ Paste thủ công vào biến `user_token`
3. ❌ Copy `userId` từ response
4. ❌ Paste vào biến `user_id`
5. ❌ Copy `order_id`, `notification_id`...
6. ❌ Lặp lại cho mỗi request

**→ Mất thời gian và dễ sai sót!**

---

## ✅ Giải Pháp: Tự Động Lưu Biến với Test Scripts

Collection `Push_Notification_Tests.postman_collection.json` đã được cấu hình **tự động lưu tất cả biến** cần thiết.

### **Cách Hoạt Động:**
Mỗi request quan trọng có **Test Script** để:
1. Parse response JSON
2. Extract giá trị cần thiết (token, userId, orderId...)
3. **Tự động lưu** vào Collection Variables
4. Log ra console để kiểm tra

---

## 📋 Danh Sách Biến Tự Động

| Biến | Được Lưu Từ Request | Sử Dụng Cho |
|------|---------------------|-------------|
| `user_token` | Login as User | Authorization header các API user |
| `admin_token` | Login as Admin | Authorization header các API admin |
| `user_id` | Login as User | Path parameter trong APIs |
| `notification_id` | Get Notifications / Get Unread | Mark as Read notification |
| `order_id` | Get User Orders | Update Order Status |

---

## 🎯 Cách Sử Dụng

### **Bước 1: Import Collection**
```
1. Mở Postman
2. Click Import → Chọn Push_Notification_Tests.postman_collection.json
3. Collection xuất hiện trong sidebar
```

### **Bước 2: Chạy Requests Theo Thứ Tự**

#### **1️⃣ Login để Lấy Token**
```
Folder: 6. Auth (Get Tokens First)

Request 1: "Login as User"
→ Gọi API với email/password
→ ✅ Tự động lưu: user_token, user_id
→ Check console: "✅ Token saved: eyJhbG..."

Request 2: "Login as Admin"  
→ Gọi API với admin credentials
→ ✅ Tự động lưu: admin_token
→ Check console: "✅ Admin token saved"
```

#### **2️⃣ Register FCM Token**
```
Folder: 1. FCM Token Management

Request: "Register FCM Token"
→ Sử dụng {{user_token}} và {{user_id}} đã lưu
→ Không cần copy/paste gì cả!
```

#### **3️⃣ Get Order ID (Nếu Cần Test Order Updates)**
```
Folder: 5. Helper - Get IDs

Request: "Get User Orders"
→ ✅ Tự động lấy order_id từ order đầu tiên
→ Check console: "✅ order_id saved: abc-123..."

Nếu không có order:
Request: "Create Test Order"
→ ✅ Tự động lưu order_id của order mới
```

#### **4️⃣ Test Order Notifications**
```
Folder: 4. Order Status Updates

Request: "Update Order to CONFIRMED"
→ Sử dụng {{admin_token}} và {{order_id}} đã lưu
→ 🔔 Mobile app nhận notification!
→ Không cần set biến thủ công
```

#### **5️⃣ Get Notification ID**
```
Folder: 2. Notification History

Request: "Get User Notifications"
→ ✅ Tự động lưu notification_id của notification đầu tiên
→ Check console: "✅ notification_id saved: xyz-456..."

Request: "Mark Notification as Read"
→ Sử dụng {{notification_id}} đã lưu
→ Không cần copy/paste!
```

---

## 🔍 Kiểm Tra Biến Đã Lưu

### **Cách 1: Xem Collection Variables**
```
1. Click vào Collection "Push Notification Tests"
2. Tab "Variables"
3. Xem cột "Current Value":
   - user_token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   - user_id: 123e4567-e89b-12d3-a456-426614174000
   - order_id: 789e0123-e45b-67c8-d901-234567890abc
   - notification_id: def4567-890a-1234-bcde-567890abcdef
```

### **Cách 2: Xem Console Output**
```
1. View → Show Postman Console (Alt + Ctrl + C)
2. Gọi API "Login as User"
3. Console hiển thị:
   ✅ Token saved: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ✅ user_id saved: 123e4567-e89b-12d3-a456-426614174000
```

---

## 🛠️ Cách Test Scripts Hoạt Động

### **Ví Dụ 1: Login as User**

**Request:**
```http
POST http://localhost:8080/api/auth/login
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "email": "user@example.com"
  }
}
```

**Test Script (Tab "Tests"):**
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    if (jsonData.data && jsonData.data.accessToken) {
        // Lưu token
        pm.collectionVariables.set('user_token', jsonData.data.accessToken);
        // Lưu userId
        pm.collectionVariables.set('user_id', jsonData.data.userId);
        // Log để debug
        console.log('✅ Token saved:', jsonData.data.accessToken);
        console.log('✅ user_id saved:', jsonData.data.userId);
    }
}
```

**Kết Quả:**
- ✅ Biến `user_token` = "eyJhbGci..."
- ✅ Biến `user_id` = "123e4567..."
- ✅ Console log: "✅ Token saved: ..."

---

### **Ví Dụ 2: Get User Notifications**

**Request:**
```http
GET http://localhost:8080/api/users/{{user_id}}/notifications?page=0&size=20
Authorization: Bearer {{user_token}}
```

**Response:**
```json
{
  "content": [
    {
      "id": "notif-123",
      "title": "Order Confirmed",
      "body": "Your order has been confirmed",
      "readAt": null
    },
    {
      "id": "notif-456",
      "title": "Order Ready",
      "body": "Your order is ready for pickup",
      "readAt": "2025-11-04T10:30:00Z"
    }
  ],
  "totalElements": 2
}
```

**Test Script:**
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    if (jsonData.content && jsonData.content.length > 0) {
        // Lấy notification đầu tiên
        var firstNotification = jsonData.content[0];
        // Lưu notification_id
        pm.collectionVariables.set('notification_id', firstNotification.id);
        // Log
        console.log('✅ notification_id saved:', firstNotification.id);
    }
}
```

**Kết Quả:**
- ✅ Biến `notification_id` = "notif-123"
- ✅ Console log: "✅ notification_id saved: notif-123"
- ✅ Request "Mark as Read" có thể dùng `{{notification_id}}` ngay

---

### **Ví Dụ 3: Get User Orders**

**Request:**
```http
GET http://localhost:8080/api/orders?userId={{user_id}}&page=0&size=1
Authorization: Bearer {{user_token}}
```

**Response:**
```json
{
  "content": [
    {
      "id": "order-789",
      "status": "PENDING",
      "totalAmount": 150000,
      "createdAt": "2025-11-04T09:00:00Z"
    }
  ]
}
```

**Test Script:**
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    if (jsonData.content && jsonData.content.length > 0) {
        pm.collectionVariables.set('order_id', jsonData.content[0].id);
        console.log('✅ order_id saved:', jsonData.content[0].id);
    } else {
        console.log('⚠️ No orders found. Please create an order first.');
    }
}
```

**Kết Quả:**
- ✅ Biến `order_id` = "order-789"
- ✅ Request "Update Order Status" có thể dùng `{{order_id}}` ngay

---

## 🎬 Workflow Hoàn Chỉnh (End-to-End Test)

### **Test Push Notification với Mobile App:**

```
1️⃣ Login
   Request: Login as User
   → ✅ Auto-save: user_token, user_id

2️⃣ Get Order ID
   Request: Get User Orders
   → ✅ Auto-save: order_id
   
   Nếu không có order:
   Request: Create Test Order
   → ✅ Auto-save: order_id

3️⃣ Register FCM Token (từ Mobile App)
   Request: Register FCM Token
   Body: { "fcmToken": "paste_token_from_mobile_app" }
   Uses: {{user_token}}, {{user_id}}
   → ✅ Backend lưu token vào database

4️⃣ Login as Admin
   Request: Login as Admin
   → ✅ Auto-save: admin_token

5️⃣ Update Order Status
   Request: Update Order to CONFIRMED
   Uses: {{admin_token}}, {{order_id}}
   → 🔔 Mobile app nhận notification!
   
6️⃣ Check Notification History
   Request: Get User Notifications
   Uses: {{user_token}}, {{user_id}}
   → ✅ Auto-save: notification_id
   → List notifications trong app

7️⃣ Mark as Read
   Request: Mark Notification as Read
   Uses: {{user_token}}, {{notification_id}}
   → ✅ Notification marked as read

8️⃣ Check Unread Count
   Request: Get Unread Count
   Uses: {{user_token}}, {{user_id}}
   → Response: { "unreadCount": 0 }
```

**→ Toàn bộ workflow KHÔNG CẦN copy/paste biến thủ công!**

---

## 🔧 Tùy Chỉnh Test Scripts

### **Nếu Response Structure Khác**

Giả sử API Login trả về structure khác:
```json
{
  "token": "eyJhbGci...",
  "user": {
    "id": "123e4567..."
  }
}
```

**Sửa Test Script:**
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    // Sửa path đến token
    pm.collectionVariables.set('user_token', jsonData.token);
    // Sửa path đến userId
    pm.collectionVariables.set('user_id', jsonData.user.id);
}
```

### **Lưu Thêm Biến Tùy Chỉnh**

**Ví dụ: Lưu email, role:**
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.collectionVariables.set('user_token', jsonData.data.accessToken);
    pm.collectionVariables.set('user_id', jsonData.data.userId);
    
    // Lưu thêm biến khác
    pm.collectionVariables.set('user_email', jsonData.data.email);
    pm.collectionVariables.set('user_role', jsonData.data.role);
    
    console.log('✅ Email:', jsonData.data.email);
    console.log('✅ Role:', jsonData.data.role);
}
```

### **Handle Errors**

```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    if (jsonData.data && jsonData.data.accessToken) {
        pm.collectionVariables.set('user_token', jsonData.data.accessToken);
        console.log('✅ Token saved');
    } else {
        console.log('❌ No token in response');
    }
} else {
    console.log('❌ Login failed:', pm.response.code);
    console.log('Error:', pm.response.json());
}
```

---

## 📊 So Sánh: Thủ Công vs Tự Động

### **Cách Cũ (Thủ Công):**
```
1. Gọi Login API
2. Copy token từ response: "eyJhbGci..."
3. Click Collection → Variables
4. Paste vào user_token
5. Copy userId: "123e4567..."
6. Paste vào user_id
7. Click Save
8. Gọi Register FCM Token
9. Gọi Get Orders
10. Copy order_id
11. Paste vào biến order_id
12. Click Save
13. Gọi Update Order Status

⏱️ Thời gian: ~3-5 phút
❌ Dễ paste nhầm
❌ Quên click Save
```

### **Cách Mới (Tự Động):**
```
1. Gọi Login API
   → ✅ Token, userId tự động lưu
2. Gọi Register FCM Token
   → ✅ Dùng biến đã lưu
3. Gọi Get Orders
   → ✅ order_id tự động lưu
4. Gọi Update Order Status
   → ✅ Dùng biến đã lưu

⏱️ Thời gian: ~30 giây
✅ Không paste sai
✅ Không quên save
✅ Làm việc nhanh hơn 10 lần
```

---

## ❓ FAQs

### **Q1: Tại sao console không log ra gì?**
```
A: Mở Postman Console:
   - View → Show Postman Console (Alt + Ctrl + C)
   - Hoặc click icon "Console" ở bottom-left
```

### **Q2: Biến không được lưu?**
```
A: Check:
   1. Response có đúng structure không? (check tab "Body")
   2. HTTP status code = 200? (check tab "Status")
   3. Test script có lỗi syntax không? (check Console)
```

### **Q3: Làm sao biết biến nào đã lưu?**
```
A: Click vào Collection → Tab "Variables"
   - Biến có giá trị = đã lưu
   - Biến trống = chưa gọi API tương ứng
```

### **Q4: Có thể export biến ra file không?**
```
A: Có! 
   1. Click Collection → Tab "Variables"
   2. Click "..." → Export
   3. Lưu file JSON
   4. Share với team members
```

### **Q5: Làm sao clear tất cả biến?**
```
A: Click Collection → Tab "Variables"
   → Xóa giá trị trong cột "Current Value"
   → Click Save
```

---

## 🎓 Best Practices

### **1. Luôn Check Console**
```javascript
// Add logs để debug
pm.collectionVariables.set('user_token', token);
console.log('✅ Token saved:', token);
console.log('✅ Token length:', token.length);
```

### **2. Validate Before Save**
```javascript
if (jsonData.data && jsonData.data.accessToken) {
    var token = jsonData.data.accessToken;
    
    // Validate token format
    if (token.startsWith('eyJ')) {
        pm.collectionVariables.set('user_token', token);
        console.log('✅ Valid JWT token saved');
    } else {
        console.log('❌ Invalid token format');
    }
}
```

### **3. Handle Edge Cases**
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    
    // Check if array is empty
    if (Array.isArray(jsonData.content) && jsonData.content.length > 0) {
        pm.collectionVariables.set('order_id', jsonData.content[0].id);
    } else {
        console.log('⚠️ No orders found. Create an order first.');
    }
}
```

### **4. Use Descriptive Logs**
```javascript
console.log('=== LOGIN SUCCESSFUL ===');
console.log('Token:', token.substring(0, 20) + '...');
console.log('User ID:', userId);
console.log('Email:', email);
console.log('========================');
```

---

## 🚀 Kết Luận

**Collection đã cấu hình tự động lưu biến:**
- ✅ `user_token` từ Login as User
- ✅ `admin_token` từ Login as Admin
- ✅ `user_id` từ Login response
- ✅ `order_id` từ Get User Orders
- ✅ `notification_id` từ Get Notifications

**Bạn chỉ cần:**
1. Import collection
2. Gọi APIs theo thứ tự
3. Tất cả biến tự động lưu
4. Không cần copy/paste thủ công!

**→ Tiết kiệm 90% thời gian test API! 🎉**

---

**Created**: November 2025  
**Version**: 1.0  
**Author**: BE_SWD Team

