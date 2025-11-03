# 📚 Push Notification - Documentation Index

## 📖 Tài Liệu Hướng Dẫn

Hệ thống bao gồm 3 tài liệu chính:

### 1️⃣ **MOBILE_PUSH_NOTIFICATION_TEST_GUIDE.md**
📱 **Hướng dẫn test Push Notification với Mobile App**

**Nội dung:**
- ✅ Setup Firebase cho Flutter/React Native
- ✅ Test notification với mobile app thật
- ✅ Kịch bản test end-to-end
- ✅ Troubleshooting chi tiết
- ✅ Best practices

**Dành cho:** Mobile Developers, QA Testers

**[→ Xem tài liệu chi tiết](./MOBILE_PUSH_NOTIFICATION_TEST_GUIDE.md)**

---

### 2️⃣ **POSTMAN_AUTO_VARIABLES_GUIDE.md**
🤖 **Hướng dẫn tự động lưu biến môi trường trong Postman**

**Nội dung:**
- ✅ Tự động lưu token, userId, orderId, notificationId
- ✅ Test Scripts hoạt động như thế nào
- ✅ So sánh thủ công vs tự động
- ✅ Tùy chỉnh scripts theo nhu cầu
- ✅ FAQs và troubleshooting

**Dành cho:** Backend Developers, QA Testers, API Testers

**[→ Xem tài liệu chi tiết](./POSTMAN_AUTO_VARIABLES_GUIDE.md)**

---

### 3️⃣ **Push_Notification_Tests.postman_collection.json**
📦 **Postman Collection với Test Scripts tự động**

**Nội dung:**
- ✅ 6 folders chính với 15+ requests
- ✅ Tự động lưu tất cả biến cần thiết
- ✅ Test scripts cho mọi API quan trọng
- ✅ Console logs để debug

**Cách sử dụng:**
1. Import vào Postman
2. Chạy "Login as User" trước
3. Các requests khác tự động dùng biến đã lưu

**[→ Xem file collection](./Push_Notification_Tests.postman_collection.json)**

---

## 🚀 Quick Start

### **Option 1: Test với Postman (Backend Only)**
```bash
1. Import Push_Notification_Tests.postman_collection.json vào Postman
2. Đọc POSTMAN_AUTO_VARIABLES_GUIDE.md
3. Chạy APIs theo thứ tự trong guide
4. Kiểm tra console logs và variables
```

### **Option 2: Test với Mobile App (Full Integration)**
```bash
1. Setup mobile app theo MOBILE_PUSH_NOTIFICATION_TEST_GUIDE.md
2. Import Postman collection
3. Mobile: Login → Get FCM token
4. Postman: Register FCM token
5. Postman: Update order status
6. Mobile: Nhận notification! 🎉
```

---

## 📂 Structure Tài Liệu

```
📁 BE_SWD/
│
├── 📄 MOBILE_PUSH_NOTIFICATION_TEST_GUIDE.md
│   ├── 1. Tổng Quan
│   ├── 2. Chuẩn Bị
│   ├── 3. Test với Postman (Backend)
│   ├── 4. Test với Mobile App
│   │   ├── Setup Firebase
│   │   ├── Flutter Example
│   │   ├── React Native Example
│   │   └── Test Flows
│   ├── 5. Kịch Bản Test Chi Tiết
│   └── 6. Troubleshooting
│
├── 📄 POSTMAN_AUTO_VARIABLES_GUIDE.md
│   ├── 1. Vấn Đề & Giải Pháp
│   ├── 2. Danh Sách Biến Tự Động
│   ├── 3. Cách Sử Dụng
│   ├── 4. Test Scripts Hoạt Động
│   ├── 5. Workflow Hoàn Chỉnh
│   ├── 6. Tùy Chỉnh Scripts
│   └── 7. FAQs
│
├── 📄 Push_Notification_Tests.postman_collection.json
│   ├── 1. FCM Token Management
│   ├── 2. Notification History
│   ├── 3. Admin - Promotional Notifications
│   ├── 4. Order Status Updates (Auto Trigger)
│   ├── 5. Helper - Get IDs
│   └── 6. Auth (Get Tokens First)
│
└── 📄 PUSH_NOTIFICATION_DOCS_INDEX.md (this file)
```

---

## 🎯 Use Cases

### **Use Case 1: Backend Developer muốn test API**
```
1. Đọc: POSTMAN_AUTO_VARIABLES_GUIDE.md
2. Import: Push_Notification_Tests.postman_collection.json
3. Test: APIs với biến tự động
4. Result: APIs work correctly ✅
```

### **Use Case 2: Mobile Developer muốn integrate push notification**
```
1. Đọc: MOBILE_PUSH_NOTIFICATION_TEST_GUIDE.md (Section 4: Test với Mobile App)
2. Setup: Firebase trong mobile app
3. Code: FCM token registration, listeners, handlers
4. Test: Với Postman collection
5. Result: App nhận notifications ✅
```

### **Use Case 3: QA Tester muốn test end-to-end**
```
1. Đọc: Cả 2 guides
2. Setup: Backend + Mobile app
3. Test: Theo kịch bản trong MOBILE_PUSH_NOTIFICATION_TEST_GUIDE.md (Section 5)
4. Result: Full flow works ✅
```

### **Use Case 4: Team Lead muốn review implementation**
```
1. Đọc: MOBILE_PUSH_NOTIFICATION_TEST_GUIDE.md (Section 1: Tổng Quan)
2. Check: Postman collection structure
3. Review: Test scripts trong collection
4. Result: Understand architecture ✅
```

---

## 🔗 API Endpoints Reference

### **Authentication**
```
POST /api/auth/login - Login user/admin
```

### **FCM Token Management**
```
PUT    /api/users/{userId}/fcm-token    - Register/update token
DELETE /api/users/{userId}/fcm-token    - Remove token (logout)
```

### **Notification History**
```
GET /api/users/{userId}/notifications              - Get all (paginated)
GET /api/users/{userId}/notifications/unread       - Get unread only
GET /api/users/{userId}/notifications/unread-count - Count unread
PUT /api/notifications/{notificationId}/read       - Mark one as read
PUT /api/users/{userId}/notifications/read-all     - Mark all as read
```

### **Promotional Notifications (Admin)**
```
POST /api/notifications/promotion - Send promo to users
```

### **Order Status Updates (Auto-trigger Notifications)**
```
POST /api/orders/{orderId}/status - Update status → Auto send notification
```

---

## 📊 Notification Types

| Type | Trigger | User Receives |
|------|---------|---------------|
| **ORDER_UPDATE** | Admin updates order status | "Order Confirmed", "Chef is Cooking", "Order Ready", etc. |
| **PROMOTION** | Admin sends promotional notification | "Flash Sale 50% OFF", "Welcome Bonus", etc. |

---

## 🎓 Learning Path

### **For Beginners:**
```
Day 1: Đọc POSTMAN_AUTO_VARIABLES_GUIDE.md
       → Hiểu cách Postman collection hoạt động
       → Test APIs trong Postman

Day 2: Đọc MOBILE_PUSH_NOTIFICATION_TEST_GUIDE.md (Backend section)
       → Hiểu flow của push notification
       → Test notification từ backend

Day 3: Đọc MOBILE_PUSH_NOTIFICATION_TEST_GUIDE.md (Mobile section)
       → Setup Firebase trong mobile app
       → Test nhận notification

Day 4: Test end-to-end scenarios
       → Login flow
       → Order flow
       → Promotion flow
```

### **For Experienced Developers:**
```
Step 1: Skim qua MOBILE_PUSH_NOTIFICATION_TEST_GUIDE.md
Step 2: Import Postman collection
Step 3: Setup mobile app theo Flutter/React Native examples
Step 4: Test end-to-end ngay
⏱️ Time: ~2 hours
```

---

## 🛠️ Tools Required

### **Backend Testing:**
- ✅ Postman (hoặc Insomnia, REST Client)
- ✅ Backend running (localhost:8080 hoặc production)
- ✅ Database access (để check data)

### **Mobile Testing:**
- ✅ Flutter/React Native development environment
- ✅ Android Studio/Xcode
- ✅ Physical device hoặc emulator with Google Play Services
- ✅ Firebase project with Cloud Messaging enabled

### **Debugging:**
- ✅ Postman Console (View → Show Postman Console)
- ✅ Backend logs (terminal hoặc log file)
- ✅ Mobile app logs (Flutter: `flutter logs`, RN: `npx react-native log-android`)
- ✅ Firebase Console (Cloud Messaging section)

---

## ❓ Common Questions

### **Q: Tôi nên đọc tài liệu nào trước?**
```
A: Depends on role:
   - Backend Developer: POSTMAN_AUTO_VARIABLES_GUIDE.md first
   - Mobile Developer: MOBILE_PUSH_NOTIFICATION_TEST_GUIDE.md first
   - QA Tester: Both guides equally important
```

### **Q: Có thể test mà không cần mobile app không?**
```
A: Có! Dùng Postman để test:
   - FCM token management APIs
   - Notification history APIs
   - Promotional notification sending
   
   Không test được:
   - Mobile app nhận notification thật
   - Notification tap handling
   - UI/UX của notifications
```

### **Q: Làm sao biết notification có gửi thành công?**
```
A: Check 3 điểm:
   1. Backend logs: "Successfully sent notification to user..."
   2. Database: notifications table có record mới với deliverySuccess = true
   3. Mobile app: Nhận được notification
```

### **Q: Postman collection có work với cả local và production không?**
```
A: Có! Chỉ cần sửa biến base_url:
   - Local: http://localhost:8080
   - Production: https://api.yourdomain.com
```

---

## 🐛 Troubleshooting

### **Issue: Notification không đến mobile app**
```
→ Xem: MOBILE_PUSH_NOTIFICATION_TEST_GUIDE.md
         Section: Troubleshooting > Problem 1
```

### **Issue: Postman biến không tự động lưu**
```
→ Xem: POSTMAN_AUTO_VARIABLES_GUIDE.md
         Section: FAQs > Q2: Biến không được lưu?
```

### **Issue: FCM token không hợp lệ**
```
→ Xem: MOBILE_PUSH_NOTIFICATION_TEST_GUIDE.md
         Section: Troubleshooting > Problem 3: Error Handling
```

---

## 📞 Support & Contribution

### **Báo Lỗi:**
```
1. Check troubleshooting sections trong guides
2. Check Postman Console và backend logs
3. Tạo issue với:
   - Mô tả lỗi
   - Steps to reproduce
   - Logs/screenshots
```

### **Đóng Góp:**
```
1. Fork repository
2. Thêm test cases vào Postman collection
3. Update guides nếu cần
4. Create pull request
```

### **Contact:**
```
- Team: BE_SWD Development Team
- Email: [Your Email]
- Slack: #push-notification-support
```

---

## 📝 Changelog

### **Version 2.0 - November 2025**
- ✅ Added MOBILE_PUSH_NOTIFICATION_TEST_GUIDE.md
- ✅ Added POSTMAN_AUTO_VARIABLES_GUIDE.md
- ✅ Updated Postman collection with auto-save scripts
- ✅ Added Flutter & React Native examples
- ✅ Comprehensive troubleshooting sections

### **Version 1.0 - October 2025**
- ✅ Initial Postman collection
- ✅ Basic push notification APIs

---

## 🎉 Kết Luận

**3 tài liệu này cung cấp đầy đủ:**
- ✅ Hướng dẫn test backend APIs (Postman)
- ✅ Hướng dẫn integrate mobile app (Flutter/React Native)
- ✅ Tự động hóa việc test (Auto-save variables)
- ✅ Troubleshooting và best practices
- ✅ End-to-end test scenarios

**→ Giúp team phát triển và test push notification nhanh chóng, chính xác! 🚀**

---

**Last Updated**: November 4, 2025  
**Maintained by**: BE_SWD Team  
**License**: Internal Use Only

