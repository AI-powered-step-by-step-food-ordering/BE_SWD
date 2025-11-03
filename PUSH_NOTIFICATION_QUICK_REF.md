# 📋 Push Notification - Quick Reference

## 🎯 Mục Đích

Hướng dẫn test và verify push notification hoạt động với mobile app.

---

## 📝 TL;DR (Too Long; Didn't Read)

### **Test với Postman (5 phút):**
```bash
1. Import Push_Notification_Tests.postman_collection.json
2. Gọi "Login as User" → Token tự động lưu ✅
3. Gọi "Register FCM Token" → Đăng ký token
4. Gọi "Login as Admin" → Admin token tự động lưu ✅
5. Gọi "Get User Orders" → order_id tự động lưu ✅
6. Gọi "Update Order to CONFIRMED" → Notification gửi đi!
```

### **Test với Mobile App (30 phút):**
```bash
1. Setup Firebase (google-services.json / GoogleService-Info.plist)
2. Code để get FCM token trong app
3. Call API: PUT /api/users/{userId}/fcm-token với token từ app
4. Update order status trong Postman
5. App nhận notification! 🎉
```

---

## 📚 Tài Liệu Chi Tiết

| File | Dành Cho | Mục Đích |
|------|----------|----------|
| **PUSH_NOTIFICATION_DOCS_INDEX.md** | Everyone | Tổng quan tất cả tài liệu |
| **MOBILE_PUSH_NOTIFICATION_TEST_GUIDE.md** | Mobile Dev, QA | Test với mobile app thật |
| **POSTMAN_AUTO_VARIABLES_GUIDE.md** | Backend Dev, QA | Hiểu cách tự động lưu biến |
| **Push_Notification_Tests.postman_collection.json** | Everyone | Collection để test APIs |

---

## 🔥 Hot Tips

### **Tip 1: Biến Tự Động Lưu**
```
Các request quan trọng TỰ ĐỘNG LƯU biến:
✅ Login as User → user_token, user_id
✅ Login as Admin → admin_token  
✅ Get User Orders → order_id
✅ Get Notifications → notification_id

→ KHÔNG CẦN copy/paste thủ công!
```

### **Tip 2: Check Console**
```
Mở Postman Console (Alt + Ctrl + C) để xem:
✅ Token saved: eyJhbGci...
✅ order_id saved: abc-123...
✅ notification_id saved: xyz-456...
```

### **Tip 3: Test Order Notifications**
```
Update order status → Auto gửi notification:
- CONFIRMED → "Order Confirmed!"
- PREPARING → "Chef is Cooking!"
- READY → "Order Ready!"
- COMPLETED → "Enjoy Your Meal!"
- CANCELLED → "Order Cancelled"
```

### **Tip 4: Mobile App Setup**
```
Flutter:
- pubspec.yaml: firebase_messaging: ^14.7.6
- Get token: await FirebaseMessaging.instance.getToken()

React Native:  
- npm install @react-native-firebase/messaging
- Get token: await messaging().getToken()
```

---

## 🐛 Common Issues

### **"Mobile app không nhận notification"**
```
Check:
1. FCM token có null không? (app logs)
2. Token đã register chưa? (database users.fcm_token)
3. Backend có log "Successfully sent"? (backend logs)
4. Firebase config đúng chưa? (google-services.json / GoogleService-Info.plist)
```

### **"Postman biến không tự động lưu"**
```
Check:
1. Mở Postman Console xem có log "✅ Token saved"?
2. Response có đúng structure không?
3. HTTP status = 200?
4. Test Script có syntax error?
```

### **"Backend log: UNREGISTERED token"**
```
Fix:
1. Token không hợp lệ (user uninstall app)
2. Backend tự động xóa token khỏi database
3. User login lại → Register token mới
```

---

## 🎬 Demo Scenario

```
SCENARIO: User đặt món và nhận notifications

1. User mở app → Login
   App: Get FCM token = "eXVR7fG..."
   App: Call PUT /api/users/{userId}/fcm-token
   ✅ Backend lưu token

2. User tạo order (Healthy Bowl)
   App: Call POST /api/orders
   ✅ Order created, status = PENDING

3. Admin confirm order
   Postman: Call POST /api/orders/{orderId}/status
           Body: { "status": "CONFIRMED" }
   🔔 Backend gửi notification
   ✅ Mobile app nhận: "✅ Order Confirmed!"

4. Kitchen preparing
   Postman: Update status → PREPARING
   🔔 Mobile app nhận: "👨‍🍳 Chef is Cooking!"

5. Order ready
   Postman: Update status → READY
   🔔 Mobile app nhận: "🎉 Order Ready!"

6. User check notifications history
   App: Call GET /api/users/{userId}/notifications
   ✅ Display 3 notifications với timestamps

7. User tap notification
   App: Mark as read
   App: Call PUT /api/notifications/{notificationId}/read
   App: Navigate to Order Detail page
   ✅ Badge count giảm từ 3 → 2
```

---

## ✅ Checklist Test Push Notification

### **Backend Setup**
- [ ] Backend running (localhost:8080 hoặc production)
- [ ] Firebase Admin SDK configured (application.yml)
- [ ] Database có tables: users, orders, notifications
- [ ] Test accounts: 1 user + 1 admin

### **Postman Setup**
- [ ] Import Push_Notification_Tests.postman_collection.json
- [ ] Open Postman Console (View → Show Postman Console)
- [ ] Call "Login as User" → Check console: "✅ Token saved"
- [ ] Call "Login as Admin" → Check console: "✅ Admin token saved"

### **Mobile App Setup** (Optional, for full test)
- [ ] Firebase project created
- [ ] google-services.json (Android) or GoogleService-Info.plist (iOS) added
- [ ] firebase_messaging dependency added
- [ ] Permission requested, status = AUTHORIZED
- [ ] FCM token retrieved (check logs)

### **API Tests**
- [ ] Register FCM token → Response 200
- [ ] Get notifications → Response 200, list displayed
- [ ] Get unread count → Response 200, count correct
- [ ] Mark as read → Response 200, count decreased
- [ ] Update order status → Response 200, notification sent

### **Mobile App Tests** (Optional)
- [ ] App foreground → Notification displayed
- [ ] App background → Notification displayed
- [ ] App terminated → Notification displayed
- [ ] Tap notification → Navigate to correct page
- [ ] Logout → Token removed → No more notifications

---

## 📞 Need Help?

1. **Quick issue?** → Check MOBILE_PUSH_NOTIFICATION_TEST_GUIDE.md (Troubleshooting section)
2. **Postman problem?** → Check POSTMAN_AUTO_VARIABLES_GUIDE.md (FAQs section)
3. **General overview?** → Check PUSH_NOTIFICATION_DOCS_INDEX.md
4. **Still stuck?** → Contact team on Slack #push-notification-support

---

## 🎓 Next Steps

### **After Basic Test:**
```
1. Test edge cases (invalid token, no internet, etc.)
2. Test performance (send to 1000 users)
3. Test different notification types (promo, system alerts)
4. Test notification scheduling (future feature)
```

### **For Production:**
```
1. Setup production Firebase project
2. Update base_url to production API
3. Test on real devices with production data
4. Monitor logs and metrics
5. Setup alerts for notification failures
```

---

**Created**: November 4, 2025  
**Version**: 1.0  
**Quick Reference - BE_SWD Team**

