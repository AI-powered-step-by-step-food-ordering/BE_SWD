# 📊 Hướng Dẫn Sử Dụng Diagrams

## Tổng Quan

Dự án có 2 loại diagram chính đã được **đơn giản hóa và tối ưu** để dễ đọc, dễ hiểu:

### 1. Activity Diagrams (4 diagrams)
📁 File: `05_Activity_Diagrams.md`

Mô tả các **quy trình nghiệp vụ** chính của hệ thống:

1. ✅ **User Registration and Email Verification** - Đăng ký và xác thực email
2. 🍜 **Create Order Process** - Tạo đơn hàng custom bowl
3. 📦 **Order Status Management** - Quản lý vòng đời đơn hàng
4. 💰 **ZaloPay Payment Processing** - Thanh toán qua ZaloPay

### 2. State Diagrams (2 diagrams)
📁 File: `06_State_Diagrams.md`

Mô tả các **trạng thái** của các đối tượng chính:

1. 📦 **Order State Machine** - Các trạng thái của đơn hàng
2. 💳 **Payment Transaction State Machine** - Các trạng thái thanh toán

---

## 🎨 Cải Tiến So Với Phiên Bản Cũ

### ✨ Activity Diagrams

**Trước (Cũ):**
- ❌ Quá nhiều chi tiết, phức tạp
- ❌ Nhiều fork/join rối rắm
- ❌ Khó theo dõi luồng chính
- ❌ Thiếu màu sắc phân biệt

**Sau (Mới):**
- ✅ Đơn giản hóa luồng, chỉ giữ logic chính
- ✅ Sử dụng màu sắc rõ ràng (Success/Error/Processing)
- ✅ Thêm swimlanes (Customer/System/Staff)
- ✅ Thêm API endpoints trong notes
- ✅ Emoji icons cho dễ nhớ (🔔 ✅ 👨‍🍳 🎉 ✨)

### ✨ State Diagrams

**Trước (Cũ):**
- ❌ Quá nhiều nested states
- ❌ Quá nhiều trạng thái phụ không cần thiết
- ❌ Notes quá dài dòng

**Sau (Mới):**
- ✅ Chỉ giữ các trạng thái chính (6 states cho Order, 7 states cho Payment)
- ✅ Color-coded theo tính chất (Pending/Processing/Success/Error)
- ✅ Notes ngắn gọn, súc tích
- ✅ Bảng State Transition Rules rõ ràng

---

## 🔍 Cách Xem Diagrams

### Option 1: PlantUML Preview trong VS Code/IntelliJ
1. Cài đặt extension: **PlantUML**
2. Mở file `.md` chứa diagram
3. Nhấn `Alt+D` hoặc click icon Preview

### Option 2: Online PlantUML Editor
1. Truy cập: https://www.plantuml.com/plantuml/uml/
2. Copy code trong `@startuml...@enduml`
3. Paste vào editor để xem

### Option 3: Export to Image
```bash
# Sử dụng PlantUML CLI
java -jar plantuml.jar diagrams/05_Activity_Diagrams.md
java -jar plantuml.jar diagrams/06_State_Diagrams.md
```

---

## 📋 Chi Tiết Từng Diagram

### 1. User Registration and Email Verification
**Mục đích:** Hiểu quy trình đăng ký tài khoản và xác thực email

**Các bước chính:**
1. User submit form (email, password, fullName)
2. System kiểm tra email tồn tại
3. Generate OTP 6 số
4. Gửi email chứa OTP (valid 5 phút)
5. User nhập OTP
6. Verify OTP và active account

**API liên quan:**
- `POST /api/auth/register`
- `POST /api/auth/verify-otp`
- `POST /api/auth/resend-otp`

---

### 2. Create Order Process
**Mục đích:** Hiểu cách customer tạo đơn hàng custom bowl

**Các bước chính:**
1. Customer chọn template và ingredients
2. System validate authentication
3. Tạo order với status = PENDING
4. Check inventory availability
5. Calculate prices (bowl price, subtotal, total)
6. Save order và send notification

**API liên quan:**
- `POST /api/orders/create`
- `GET /api/bowl-templates/getall`
- `GET /api/ingredients/getall`

---

### 3. Order Status Management
**Mục đích:** Hiểu vòng đời đơn hàng từ PENDING → COMPLETED

**Các trạng thái:**
1. **PENDING** - Đơn mới tạo, chờ confirm
2. **CONFIRMED** - Admin đã confirm, tạo kitchen job
3. **PREPARING** - Đầu bếp đang nấu
4. **READY** - Món ăn sẵn sàng, chờ khách lấy
5. **COMPLETED** - Khách đã nhận, hoàn thành
6. **CANCELLED** - Đơn bị hủy

**API liên quan:**
- `POST /api/orders/confirm/{id}`
- `POST /api/orders/cancel/{id}`
- `POST /api/orders/complete/{id}`

---

### 4. ZaloPay Payment Processing
**Mục đích:** Hiểu cách tích hợp thanh toán với ZaloPay

**Các bước chính:**
1. Customer click "Pay with ZaloPay"
2. System tạo payment transaction
3. Generate app_trans_id và HMAC signature
4. Call ZaloPay API để tạo payment URL
5. Customer mở ZaloPay app và xác nhận
6. ZaloPay gửi callback về server
7. System verify MAC signature và update order

**API liên quan:**
- `POST /api/zalopay/create-payment`
- `POST /api/zalopay/callback` (ZaloPay gọi đến)
- `POST /api/zalopay/refund`

**Security:**
- ✅ HMAC-SHA256 signature verification
- ✅ MAC callback validation
- ✅ Prevent replay attacks

---

### 5. Order State Machine
**Mục đích:** Hiểu các trạng thái và chuyển đổi của đơn hàng

**State Transitions:**
```
[*] → PENDING → CONFIRMED → PREPARING → READY → COMPLETED → [*]
                    ↓            ↓          ↓
                CANCELLED ← CANCELLED ← CANCELLED
```

**Terminal States:**
- ✅ COMPLETED - Thành công
- ❌ CANCELLED - Thất bại

---

### 6. Payment Transaction State Machine
**Mục đích:** Hiểu các trạng thái thanh toán ZaloPay

**State Transitions:**
```
[*] → PENDING → INITIATED → PROCESSING → COMPLETED → [*]
                                 ↓              ↓
                              FAILED         REFUND → REFUNDED → [*]
                                 ↓
                              EXPIRED
```

**Terminal States:**
- ✅ COMPLETED - Thanh toán thành công
- ❌ FAILED - Thanh toán thất bại (có thể retry)
- 💰 REFUNDED - Đã hoàn tiền

---

## 🎯 Use Cases Chính

### Use Case 1: Customer đặt món và thanh toán
1. Xem **Create Order Process** diagram
2. Xem **ZaloPay Payment Processing** diagram
3. Xem **Order State Machine** để hiểu flow

### Use Case 2: Admin/Staff xử lý đơn hàng
1. Xem **Order Status Management** diagram
2. Xem **Order State Machine** để biết các trạng thái
3. Follow luồng từ PENDING → COMPLETED

### Use Case 3: Customer đăng ký tài khoản mới
1. Xem **User Registration** diagram
2. Hiểu flow: Register → OTP Email → Verify → Active

### Use Case 4: Xử lý thanh toán và hoàn tiền
1. Xem **ZaloPay Payment Processing** diagram
2. Xem **Payment Transaction State Machine**
3. Hiểu refund flow khi order bị cancel

---

## 📊 Bảng So Sánh Các Trạng thái

### Order Status

| Status | Màu sắc | Ý nghĩa | Actions có thể thực hiện |
|--------|---------|---------|--------------------------|
| PENDING | 🟡 Yellow | Đơn mới, chờ xử lý | Confirm, Cancel, Apply Promo |
| CONFIRMED | 🔵 Blue | Đã xác nhận | Start Preparing, Cancel |
| PREPARING | 🟠 Orange | Đang nấu | Complete, Cancel (rare) |
| READY | 🟢 Green | Sẵn sàng lấy | Complete (pickup), Auto-cancel |
| COMPLETED | ✅ Dark Green | Hoàn thành | View, Rate |
| CANCELLED | 🔴 Red | Đã hủy | View only |

### Payment Status

| Status | Màu sắc | Ý nghĩa | Actions có thể thực hiện |
|--------|---------|---------|--------------------------|
| PENDING | 🟡 Yellow | Chưa thanh toán | Create payment URL |
| INITIATED | 🔵 Light Blue | Đã tạo URL | Wait for customer |
| PROCESSING | 🔵 Blue | Đang xử lý | Wait for callback |
| COMPLETED | ✅ Green | Thành công | Refund (nếu cần) |
| FAILED | 🔴 Red | Thất bại | Retry payment |
| REFUNDED | ⚪ Gray | Đã hoàn tiền | View only |

---

## 🔗 Liên Kết Giữa Các Diagram

```
User Registration
      ↓
  (User Active)
      ↓
Create Order → Order State Machine
      ↓              ↓
Payment Process → Payment State Machine
      ↓              ↓
Order Lifecycle → COMPLETED/CANCELLED
```

---

## 📝 Lưu Ý Khi Đọc Diagrams

### Activity Diagrams
- ⬇️ Flow từ trên xuống dưới
- 🔶 Diamond = Decision point (if/else)
- 📦 Rectangle = Action/Process
- 🟩 Green = Success
- 🟥 Red = Error
- 🟦 Blue = Processing

### State Diagrams
- ⭕ Circle = State
- ➡️ Arrow = Transition
- 📝 Note = Additional info
- 🎨 Color = State category

---

## 🚀 Next Steps

1. **Đọc các diagram theo thứ tự:**
   - Activity Diagrams trước (hiểu process)
   - State Diagrams sau (hiểu states)

2. **Đối chiếu với source code:**
   - Xem `OrderController.java`
   - Xem `ZaloPayController.java`
   - Xem `AuthController.java`

3. **Test flow:**
   - Sử dụng Postman collection
   - Follow đúng sequence trong diagram

4. **Customize:**
   - Có thể thêm business rules riêng
   - Điều chỉnh màu sắc theo brand
   - Thêm/bớt states nếu cần

---

## 📞 Support

Nếu có thắc mắc về diagrams:
1. Đọc kỹ notes trong diagram
2. Xem API endpoints tương ứng
3. Đối chiếu với source code
4. Tham khảo file `HUONG_DAN_SU_DUNG_HE_THONG.md`

---

**Version:** 2.0 (Simplified & Enhanced)  
**Last Updated:** October 31, 2025  
**Author:** Development Team

