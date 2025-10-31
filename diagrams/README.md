# 📊 Healthy Food Ordering System - UML Diagrams Documentation

Tài liệu này chứa tất cả các UML diagrams cho hệ thống đặt đồ ăn healthy food, bao gồm Use Case, Sequence, Activity, State và Class Diagrams.

## 📁 Danh Sách Diagrams

### 1. [Use Case Diagram](./01_UseCase_Diagram.md)
Mô tả tất cả các chức năng của hệ thống và tương tác giữa các actors (Customer, Admin, Staff) với hệ thống.

**Nội dung chính:**
- Authentication & User Management (8 use cases)
- Bowl & Order Management (9 use cases)
- Payment Management (3 use cases)
- Notification Management (4 use cases)
- Admin Management (7 use cases)
- Kitchen Management (4 use cases)

**Actors:**
- Customer (USER)
- Admin
- Kitchen Staff (STAFF)
- ZaloPay Gateway
- Firebase FCM

---

### 2. [Sequence Diagrams - Authentication](./02_Sequence_Diagram_Authentication.md)
Mô tả chi tiết luồng xử lý authentication và quản lý user.

**Bao gồm 5 sequence diagrams:**
1. **User Registration and Email Verification** - Đăng ký tài khoản và xác thực email qua OTP
2. **User Login with FCM Token** - Đăng nhập và lưu FCM token cho push notification
3. **Forgot Password and Reset Password** - Quên mật khẩu và đặt lại mật khẩu qua OTP
4. **Logout Flow** - Đăng xuất và xóa FCM token
5. **Refresh Token Flow** - Làm mới access token bằng refresh token

---

### 3. [Sequence Diagrams - Order Management](./03_Sequence_Diagram_Order_Management.md)
Mô tả chi tiết luồng xử lý đơn hàng từ tạo đến hoàn thành.

**Bao gồm 6 sequence diagrams:**
1. **Create Order Flow** - Tạo đơn hàng với validation và notification
2. **Update Order Status Flow** - Cập nhật trạng thái đơn hàng với push notification tự động
3. **Apply Promotion to Order Flow** - Áp dụng mã khuyến mãi cho đơn hàng
4. **Complete Order Flow** - Hoàn thành đơn hàng và trừ inventory
5. **Cancel Order Flow** - Hủy đơn hàng và xử lý refund
6. **View Order History Flow** - Xem lịch sử đơn hàng

---

### 4. [Sequence Diagrams - Payment & Notification](./04_Sequence_Diagram_Payment_Notification.md)
Mô tả luồng thanh toán ZaloPay và push notification.

**Bao gồm 5 sequence diagrams:**
1. **ZaloPay Payment Flow** - Thanh toán qua ZaloPay Gateway với callback
2. **Push Notification Flow (FCM)** - Gửi push notification khi cập nhật trạng thái đơn hàng
3. **Promotional Notification Flow** - Gửi notification khuyến mãi đến nhiều users
4. **View Notification History Flow** - Xem lịch sử và quản lý notifications
5. **FCM Token Management Flow** - Quản lý FCM token (save/remove/refresh)

---

### 5. [Activity Diagrams](./05_Activity_Diagrams.md)
Mô tả các business process flows chi tiết.

**Bao gồm 7 activity diagrams:**
1. **User Registration and Verification Process** - Quy trình đăng ký và xác thực email
2. **Create Order Process** - Quy trình tạo đơn hàng từ chọn bowl đến thanh toán
3. **Order Lifecycle Process** - Vòng đời hoàn chỉnh của đơn hàng (PENDING → COMPLETED/CANCELLED)
4. **Payment Processing Activity** - Quy trình xử lý thanh toán ZaloPay
5. **Apply Promotion Code Activity** - Quy trình áp dụng mã khuyến mãi với validation
6. **Kitchen Job Management Activity** - Quy trình quản lý công việc bếp
7. **Inventory Management Activity** - Quy trình quản lý kho (Add/Deduct/Adjust/Check stock)

---

### 6. [State Diagrams](./06_State_Diagrams.md)
Mô tả state machine của các main objects trong hệ thống.

**Bao gồm 6 state diagrams:**
1. **User Account State Diagram** - States: PendingVerification → Active → Suspended/Deleted
2. **Order State Diagram** - States: Pending → Confirmed → Preparing → Ready → Completed/Cancelled
3. **Payment Transaction State Diagram** - States: Pending → Initiated → Processing → Completed/Failed/Refunded
4. **Kitchen Job State Diagram** - States: Pending → Assigned → InProgress → QualityCheck → Completed/Cancelled
5. **Promotion State Diagram** - States: Draft → Scheduled → Active → Expired/UsageLimitReached/Cancelled
6. **Notification State Diagram** - States: Created → Sending → Sent → Delivered → Opened/Read/Dismissed

---

### 7. [Sequence Diagrams - Bowl Management](./07_Sequence_Diagram_Bowl_Management.md)
Mô tả luồng quản lý bowl, ingredient và category.

**Bao gồm 4 sequence diagrams:**
1. **Create Custom Bowl Flow** - Tạo bowl tùy chỉnh từ template với validation
2. **Ingredient Management Flow (Admin)** - Admin quản lý ingredients (CRUD + restrictions)
3. **Category Management Flow** - Quản lý categories với min/max selection rules
4. **Bowl Template Creation Flow (Admin)** - Tạo bowl template với multi-step configuration

---

### 8. [Class Diagrams](./08_Class_Diagrams.md)
Mô tả cấu trúc class và architecture của hệ thống.

**Bao gồm 4 class diagrams:**
1. **High-Level System Architecture Diagram** - Tổng quan kiến trúc hệ thống (Client → API → Service → Database)
2. **Core Domain Model Class Diagram** - Mô hình entities và relationships
3. **Service Layer Class Diagram** - Cấu trúc service layer với business logic
4. **Controller Layer Class Diagram** - REST API controllers và endpoints

---

## 🔧 Công Nghệ Sử Dụng

### Backend Stack:
- **Framework:** Spring Boot 3.x
- **Database:** PostgreSQL
- **ORM:** JPA/Hibernate
- **Authentication:** JWT (JSON Web Token)
- **Payment Gateway:** ZaloPay API
- **Push Notification:** Firebase Cloud Messaging (FCM)
- **Email:** SMTP với HTML templates
- **Documentation:** OpenAPI/Swagger

### Design Patterns:
- **Unit of Work Pattern** - ServiceProvider for transaction management
- **Repository Pattern** - JPA Repositories for data access
- **DTO Pattern** - Request/Response objects
- **Mapper Pattern** - Entity ↔ DTO conversion
- **Strategy Pattern** - Payment method handling

---

## 📊 Cách Xem Diagrams

### Option 1: PlantUML Extension (Recommended)
1. Cài đặt PlantUML extension trong IDE của bạn:
   - **VS Code:** "PlantUML" by jebbs
   - **IntelliJ IDEA:** PlantUML integration plugin
   - **Eclipse:** PlantUML plugin

2. Mở file `.md` và xem preview của PlantUML code blocks

### Option 2: PlantUML Online Server
1. Copy PlantUML code từ các file `.md`
2. Paste vào [PlantUML Online Editor](http://www.plantuml.com/plantuml/uml/)
3. Xem kết quả diagram

### Option 3: Export to Images
Sử dụng PlantUML CLI để export diagrams thành PNG/SVG:
```bash
plantuml -tpng diagrams/*.md
plantuml -tsvg diagrams/*.md
```

---

## 📋 Các Entities Chính

| Entity | Description | Key States/Statuses |
|--------|-------------|---------------------|
| **User** | Người dùng hệ thống | PENDING_VERIFICATION, ACTIVE, DELETED |
| **Order** | Đơn hàng | PENDING, CONFIRMED, PREPARING, READY, COMPLETED, CANCELLED |
| **Bowl** | Món ăn trong đơn hàng | - |
| **BowlItem** | Ingredient trong bowl | - |
| **Ingredient** | Nguyên liệu | isActive: true/false |
| **Category** | Nhóm nguyên liệu | With min/max selection rules |
| **Promotion** | Khuyến mãi | Draft, Scheduled, Active, Expired, Cancelled |
| **PaymentTransaction** | Giao dịch thanh toán | PENDING, COMPLETED, FAILED, REFUNDED |
| **Notification** | Thông báo | Created, Sent, Delivered, Read |
| **KitchenJob** | Công việc bếp | PENDING, IN_PROGRESS, COMPLETED, CANCELLED |
| **Inventory** | Kho nguyên liệu | With stock actions: ADD, DEDUCT, ADJUST |

---

## 🔄 Main Business Flows

### 1. Complete Order Flow
```
Customer creates order → Admin confirms → Kitchen prepares → 
Order ready → Customer picks up → Inventory deducted → Completed
```

**Notifications sent at each step:**
- Order created: "🔔 Order Received"
- Confirmed: "✅ Order Confirmed - Estimated time: 30 mins"
- Preparing: "👨‍🍳 Chef is preparing your delicious meal!"
- Ready: "🎉 Order Ready for Pickup"
- Completed: "✨ Enjoy your meal! Rate your experience"

### 2. Payment Flow
```
Create order → Generate ZaloPay payment URL → Customer pays → 
ZaloPay callback → Update payment status → Order marked as PAID
```

### 3. Authentication Flow
```
Register → Receive OTP email → Verify OTP → Account activated → 
Login → Save FCM token → Use app → Logout → Remove FCM token
```

---

## 🎯 Key Features

### For Customers:
- ✅ Email verification with OTP
- ✅ Build custom healthy bowls with restrictions (allergen, dietary)
- ✅ Apply promotion codes
- ✅ Pay via ZaloPay
- ✅ Real-time push notifications for order status
- ✅ View order history

### For Admin:
- ✅ Manage users, ingredients, categories
- ✅ Create bowl templates with multi-step configuration
- ✅ Create and manage promotions
- ✅ Update order status
- ✅ Send promotional notifications
- ✅ View all orders and analytics

### For Kitchen Staff:
- ✅ View assigned kitchen jobs
- ✅ Update job status
- ✅ Manage inventory (add/deduct/adjust stock)
- ✅ Track low stock items

---

## 🔐 Security Features

- **JWT Authentication:** Access token (1 hour) + Refresh token (7 days)
- **Token Blacklist:** Blacklist access tokens on logout
- **OTP Verification:** 6-digit OTP with 5-minute expiry and max 5 attempts
- **Password Hashing:** BCrypt password encryption
- **CORS Configuration:** Cross-origin resource sharing
- **Role-based Access Control:** ADMIN, STAFF, USER roles

---

## 📞 API Endpoints Overview

### Authentication
- `POST /api/auth/register` - Đăng ký
- `POST /api/auth/login` - Đăng nhập
- `POST /api/auth/logout` - Đăng xuất
- `POST /api/auth/verify-otp` - Xác thực OTP
- `POST /api/auth/forgot-password` - Quên mật khẩu
- `POST /api/auth/reset-password` - Đặt lại mật khẩu
- `POST /api/auth/refresh` - Làm mới token

### Orders
- `POST /api/orders/create` - Tạo đơn hàng
- `GET /api/orders/order-history/{userId}` - Lịch sử đơn hàng
- `PUT /api/orders/{orderId}/status` - Cập nhật trạng thái
- `POST /api/orders/apply-promo/{orderId}` - Áp dụng khuyến mãi
- `POST /api/orders/confirm/{orderId}` - Xác nhận đơn
- `POST /api/orders/cancel/{orderId}` - Hủy đơn
- `POST /api/orders/complete/{orderId}` - Hoàn thành đơn

### Notifications
- `PUT /api/users/{userId}/fcm-token` - Lưu FCM token
- `DELETE /api/users/{userId}/fcm-token` - Xóa FCM token
- `GET /api/users/{userId}/notifications` - Lịch sử thông báo
- `GET /api/users/{userId}/notifications/unread` - Số thông báo chưa đọc
- `PUT /api/users/{userId}/notifications/{notificationId}/read` - Đánh dấu đã đọc
- `POST /api/notifications/promotion` - Gửi thông báo khuyến mãi

### Payment
- `POST /api/zalopay/create-order` - Tạo đơn thanh toán ZaloPay
- `POST /api/zalopay/callback` - ZaloPay callback webhook
- `GET /api/zalopay/query-status/{orderId}` - Trạng thái thanh toán

### Bowls & Ingredients
- `GET /api/bowl-templates/getall` - Danh sách bowl templates
- `POST /api/bowls/create` - Tạo bowl tùy chỉnh
- `GET /api/ingredients/getall` - Danh sách nguyên liệu
- `GET /api/categories/getall` - Danh sách categories

---

## 📝 Notes

- Tất cả diagrams được viết bằng PlantUML syntax
- Các sequence diagrams đã bao gồm error handling và alternative flows
- State diagrams mô tả đầy đủ transitions và terminal states
- Activity diagrams có swimlanes để phân biệt actors và system
- Class diagrams tuân theo UML 2.0 notation

---

## 👥 Contributors

Hệ thống được thiết kế cho dự án SWD - Software Design Course.

---

## 📅 Last Updated

October 30, 2025


