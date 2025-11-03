# 📱 Hướng Dẫn Test Push Notification với Mobile App

## 📋 Mục Lục
1. [Yêu Cầu Chuẩn Bị](#yêu-cầu-chuẩn-bị)
2. [Cấu Hình Firebase cho Mobile App](#cấu-hình-firebase-cho-mobile-app)
3. [Test với Mobile App Thật](#test-với-mobile-app-thật)
4. [Test với Postman (Development)](#test-với-postman-development)
5. [Kiểm Tra Notification Hoạt Động](#kiểm-tra-notification-hoạt-động)
6. [Troubleshooting](#troubleshooting)

---

## 📋 Yêu Cầu Chuẩn Bị

### Backend Requirements
- ✅ Backend đang chạy tại `http://localhost:8080` hoặc production URL
- ✅ Firebase Admin SDK đã được cấu hình trong `application.yml`
- ✅ Database đã sẵn sàng (PostgreSQL/MySQL)
- ✅ Đã import file `Push_Notification_Tests.postman_collection.json` (optional)

### Mobile App Requirements
- ✅ Flutter/React Native app với Firebase Messaging plugin
- ✅ App đã được config với Firebase project (google-services.json hoặc GoogleService-Info.plist)
- ✅ Đã request notification permission từ user
- ✅ Có cơ chế lấy FCM token từ device

### Testing Tools
- ✅ Postman hoặc REST Client (để test API)
- ✅ Physical device hoặc emulator có Google Play Services
- ✅ Đăng ký ít nhất 1 tài khoản USER và 1 tài khoản ADMIN

---

## 🔥 Cấu Hình Firebase cho Mobile App

### 1. Tạo Firebase Project
1. Truy cập [Firebase Console](https://console.firebase.google.com)
2. Tạo hoặc chọn project hiện có
3. Enable **Cloud Messaging API (Legacy)** hoặc **Firebase Cloud Messaging API (V1)**

### 2. Thêm App vào Firebase Project

#### **Android:**
```bash
# Download google-services.json
# Đặt vào: android/app/google-services.json
```

**build.gradle (Project level):**
```gradle
dependencies {
    classpath 'com.google.gms:google-services:4.3.15'
}
```

**build.gradle (App level):**
```gradle
apply plugin: 'com.google.gms.google-services'

dependencies {
    implementation 'com.google.firebase:firebase-messaging:23.2.1'
}
```

#### **iOS:**
```bash
# Download GoogleService-Info.plist
# Đặt vào: ios/Runner/GoogleService-Info.plist
```

**Podfile:**
```ruby
pod 'Firebase/Messaging'
```

### 3. Yêu Cầu Permissions

#### **Android (AndroidManifest.xml):**
```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
```

#### **iOS (Info.plist):**
```xml
<key>UIBackgroundModes</key>
<array>
    <string>remote-notification</string>
</array>
```

### 4. Lấy FCM Token trong App

#### **Flutter Example:**
```dart
import 'package:firebase_messaging/firebase_messaging.dart';

Future<String?> getFCMToken() async {
  FirebaseMessaging messaging = FirebaseMessaging.instance;
  
  // Request permission (iOS)
  NotificationSettings settings = await messaging.requestPermission(
    alert: true,
    badge: true,
    sound: true,
  );
  
  if (settings.authorizationStatus == AuthorizationStatus.authorized) {
    String? token = await messaging.getToken();
    print('FCM Token: $token');
    return token;
  }
  return null;
}
```

#### **React Native Example:**
```javascript
import messaging from '@react-native-firebase/messaging';

async function getFCMToken() {
  const authStatus = await messaging().requestPermission();
  const enabled =
    authStatus === messaging.AuthorizationStatus.AUTHORIZED ||
    authStatus === messaging.AuthorizationStatus.PROVISIONAL;

  if (enabled) {
    const token = await messaging().getToken();
    console.log('FCM Token:', token);
    return token;
  }
}
```

---

## 📱 Test với Mobile App Thật

### **BƯỚC 1: Đăng Ký FCM Token từ Mobile App**

#### 1.1. Login từ Mobile App
```dart
// Flutter Example
final response = await http.post(
  Uri.parse('$baseUrl/api/auth/login'),
  headers: {'Content-Type': 'application/json'},
  body: jsonEncode({
    'email': 'user@example.com',
    'password': 'password123'
  }),
);

final data = jsonDecode(response.body);
String token = data['data']['token'];
String userId = data['data']['userId'];
```

**✅ Expected Response:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "email": "user@example.com",
    "role": "USER",
    "fullName": "Nguyen Van A"
  }
}
```

#### 1.2. Lấy FCM Token và Gửi lên Server
```dart
// Get FCM token from Firebase
String? fcmToken = await FirebaseMessaging.instance.getToken();
String deviceId = await getDeviceId(); // Use device_info_plus package
String platform = Platform.isAndroid ? 'android' : 'ios';

// Send to backend
await http.put(
  Uri.parse('$baseUrl/api/users/$userId/fcm-token'),
  headers: {
    'Authorization': 'Bearer $token',
    'Content-Type': 'application/json'
  },
  body: jsonEncode({
    'fcmToken': fcmToken,
    'platform': platform,
    'deviceId': deviceId
  }),
);
```

**✅ Expected Response:**
```json
{
  "success": true,
  "message": "FCM token saved successfully",
  "data": null
}
```

**🔍 Verify trong Database:**
```sql
SELECT id, email, fcm_token, fcm_platform, fcm_device_id, fcm_token_updated_at 
FROM users 
WHERE id = 'user-uuid';
```

---

### **BƯỚC 2: Setup Notification Listener trong App**

#### Flutter Example:
```dart
import 'package:firebase_messaging/firebase_messaging.dart';

class NotificationService {
  final FirebaseMessaging _messaging = FirebaseMessaging.instance;
  
  // Initialize
  Future<void> initialize() async {
    // Request permission
    await _messaging.requestPermission(
      alert: true,
      badge: true,
      sound: true,
    );
    
    // Handle foreground messages
    FirebaseMessaging.onMessage.listen((RemoteMessage message) {
      print('Got a message whilst in the foreground!');
      print('Message data: ${message.data}');
      
      if (message.notification != null) {
        print('Title: ${message.notification!.title}');
        print('Body: ${message.notification!.body}');
        
        // Show local notification
        _showLocalNotification(message);
      }
    });
    
    // Handle background messages
    FirebaseMessaging.onMessageOpenedApp.listen((RemoteMessage message) {
      print('Message clicked!');
      _handleNotificationClick(message);
    });
    
    // Handle notification when app is terminated
    RemoteMessage? initialMessage = await _messaging.getInitialMessage();
    if (initialMessage != null) {
      _handleNotificationClick(initialMessage);
    }
  }
  
  void _handleNotificationClick(RemoteMessage message) {
    String type = message.data['type'] ?? '';
    
    switch (type) {
      case 'order_update':
        String orderId = message.data['orderId'];
        // Navigate to order detail screen
        navigatorKey.currentState?.pushNamed('/order-detail', arguments: orderId);
        break;
      case 'promotion':
        String? promoCode = message.data['promoCode'];
        // Navigate to promo screen
        navigatorKey.currentState?.pushNamed('/promotions', arguments: promoCode);
        break;
      default:
        // Navigate to notification list
        navigatorKey.currentState?.pushNamed('/notifications');
    }
  }
  
  void _showLocalNotification(RemoteMessage message) {
    // Use flutter_local_notifications to show notification
    // when app is in foreground
  }
}
```

#### React Native Example:
```javascript
import messaging from '@react-native-firebase/messaging';
import notifee from '@notifee/react-native';

// Request permission
async function requestUserPermission() {
  const authStatus = await messaging().requestPermission();
  const enabled =
    authStatus === messaging.AuthorizationStatus.AUTHORIZED ||
    authStatus === messaging.AuthorizationStatus.PROVISIONAL;
  
  if (enabled) {
    console.log('Authorization status:', authStatus);
  }
}

// Handle foreground notifications
messaging().onMessage(async remoteMessage => {
  console.log('Notification received in foreground:', remoteMessage);
  
  // Display notification using notifee
  await notifee.displayNotification({
    title: remoteMessage.notification?.title,
    body: remoteMessage.notification?.body,
    data: remoteMessage.data,
  });
});

// Handle background/quit state notifications
messaging().setBackgroundMessageHandler(async remoteMessage => {
  console.log('Message handled in the background!', remoteMessage);
});

// Handle notification click
messaging().onNotificationOpenedApp(remoteMessage => {
  console.log('Notification caused app to open from background:', remoteMessage);
  handleNotificationClick(remoteMessage);
});

// Check if app was opened from notification (quit state)
messaging()
  .getInitialNotification()
  .then(remoteMessage => {
    if (remoteMessage) {
      console.log('Notification caused app to open from quit state:', remoteMessage);
      handleNotificationClick(remoteMessage);
    }
  });

function handleNotificationClick(remoteMessage) {
  const type = remoteMessage.data?.type;
  
  switch (type) {
    case 'order_update':
      navigation.navigate('OrderDetail', { orderId: remoteMessage.data.orderId });
      break;
    case 'promotion':
      navigation.navigate('Promotions', { promoCode: remoteMessage.data.promoCode });
      break;
    default:
      navigation.navigate('Notifications');
  }
}
```

---

### **BƯỚC 3: Test Order Notifications (Tự Động)**

#### 3.1. Tạo Order từ Mobile App
```dart
// Create order
final response = await http.post(
  Uri.parse('$baseUrl/api/orders'),
  headers: {
    'Authorization': 'Bearer $userToken',
    'Content-Type': 'application/json'
  },
  body: jsonEncode({
    'storeId': 'store-uuid',
    'orderItems': [
      {
        'bowlId': 'bowl-uuid-1',
        'quantity': 2,
        'price': 50000
      }
    ],
    'totalAmount': 100000,
    'note': 'No onions please'
  }),
);

String orderId = jsonDecode(response.body)['data']['id'];
```

#### 3.2. Admin Cập Nhật Order Status

**Test Scenario: Complete Order Flow**

```bash
# Step 1: CONFIRMED
POST http://localhost:8080/api/orders/{orderId}/status
Authorization: Bearer {admin_token}
Body: {"status": "CONFIRMED"}
```
📱 **Mobile nhận notification:**
- Title: "✅ Order Confirmed"
- Body: "Order #ABC12345 confirmed! Estimated time: 30 mins"
- Data: `{"type": "order_update", "orderId": "...", "status": "confirmed"}`

```bash
# Step 2: PREPARING
Body: {"status": "PREPARING"}
```
📱 **Mobile nhận notification:**
- Title: "👨‍🍳 Chef is Cooking"
- Body: "Chef is preparing your delicious meal!"

```bash
# Step 3: READY
Body: {"status": "READY"}
```
📱 **Mobile nhận notification:**
- Title: "🎉 Order Ready!"
- Body: "Order #ABC12345 is ready for pickup at Healthy Store"

```bash
# Step 4: COMPLETED
Body: {"status": "COMPLETED"}
```
📱 **Mobile nhận notification:**
- Title: "✨ Enjoy Your Meal!"
- Body: "Order delivered! Don't forget to rate your experience"

---

### **BƯỚC 4: Test Promotional Notifications (Manual)**

#### 4.1. Admin Gửi Promotion từ Postman

```bash
POST http://localhost:8080/api/notifications/promotion
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "userIds": ["user-uuid-1", "user-uuid-2"],
  "title": "🎉 Flash Sale 50% OFF!",
  "message": "Giảm 50% tất cả món ăn healthy. Chỉ hôm nay!",
  "imageUrl": "https://example.com/flash-sale.jpg",
  "promoCode": "FLASH50"
}
```

**✅ Expected Response:**
```json
{
  "success": true,
  "message": "Promotional notifications sent",
  "data": {
    "total": 2,
    "success": 2,
    "failed": 0,
    "failedUserIds": []
  }
}
```

📱 **Mobile nhận notification với:**
- Title: "🎉 Flash Sale 50% OFF!"
- Body: "Giảm 50% tất cả món ăn healthy. Chỉ hôm nay!"
- Image: Banner promotion
- Data: `{"type": "promotion", "promoCode": "FLASH50"}`

#### 4.2. Các Promotion Templates Khác

**Welcome New User:**
```json
{
  "userIds": ["new-user-id"],
  "title": "🎊 Welcome to Healthy Food!",
  "message": "Get 20% OFF on your first order. Code: WELCOME20",
  "promoCode": "WELCOME20"
}
```

**Birthday Promotion:**
```json
{
  "userIds": ["birthday-user-id"],
  "title": "🎂 Happy Birthday!",
  "message": "Special gift: 100K voucher for you!",
  "imageUrl": "https://example.com/birthday.jpg",
  "promoCode": "BIRTHDAY100"
}
```

**Weekend Special:**
```json
{
  "userIds": ["all-active-users"],
  "title": "🌟 Weekend Special",
  "message": "Free delivery for orders over 200K this weekend!",
  "promoCode": "WEEKEND2024"
}
```

---

### **BƯỚC 5: Kiểm Tra Notification History trong App**

#### 5.1. Lấy Danh Sách Notifications
```dart
// Get notifications with pagination
final response = await http.get(
  Uri.parse('$baseUrl/api/users/$userId/notifications?page=0&size=20'),
  headers: {
    'Authorization': 'Bearer $token',
  },
);

final data = jsonDecode(response.body);
List notifications = data['content'];
```

**✅ Expected Response:**
```json
{
  "content": [
    {
      "id": "notif-uuid-1",
      "title": "🎉 Order Ready!",
      "body": "Order #ABC12345 is ready for pickup",
      "type": "ORDER_UPDATE",
      "orderStatus": "READY",
      "orderId": "order-uuid",
      "sentAt": "2025-11-03T10:30:00+07:00",
      "readAt": null,
      "isRead": false
    },
    {
      "id": "notif-uuid-2",
      "title": "🔥 Flash Sale 50% OFF!",
      "body": "Giảm 50% tất cả món ăn healthy",
      "type": "PROMOTION",
      "orderStatus": null,
      "orderId": null,
      "sentAt": "2025-11-03T09:15:00+07:00",
      "readAt": "2025-11-03T09:20:00+07:00",
      "isRead": true
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 15,
  "totalPages": 1,
  "last": true
}
```

#### 5.2. Lấy Unread Count (Badge)
```dart
// Get unread count for badge
final response = await http.get(
  Uri.parse('$baseUrl/api/users/$userId/notifications/unread-count'),
  headers: {'Authorization': 'Bearer $token'},
);

int unreadCount = jsonDecode(response.body)['unreadCount'];
// Update badge on notification icon
```

**✅ Expected Response:**
```json
{
  "unreadCount": 5
}
```

#### 5.3. Đánh Dấu Đã Đọc
```dart
// Mark single notification as read
await http.put(
  Uri.parse('$baseUrl/api/notifications/$notificationId/read'),
  headers: {'Authorization': 'Bearer $token'},
);

// Mark all as read
await http.put(
  Uri.parse('$baseUrl/api/users/$userId/notifications/read-all'),
  headers: {'Authorization': 'Bearer $token'},
);
```

---

### **BƯỚC 6: Test Logout (Remove FCM Token)**

```dart
// On logout
await http.delete(
  Uri.parse('$baseUrl/api/users/$userId/fcm-token'),
  headers: {'Authorization': 'Bearer $token'},
);

// Clear local data
await FirebaseMessaging.instance.deleteToken();
```

**✅ Expected Response:**
```json
{
  "success": true,
  "message": "FCM token removed successfully",
  "data": null
}
```

**🔍 Verify:** User sẽ KHÔNG nhận notification sau khi logout ✅

---

## ⚠️ LƯU Ý QUAN TRỌNG

### ❌ Token Giả KHÔNG Hoạt Động
Nếu test với token giả như `test_fcm_token_abc123xyz`, backend sẽ trả về:
```json
{
  "success": true,
  "data": {
    "total": 1,
    "success": 0,
    "failed": 1,
    "failedUserIds": ["user-id"]
  }
}
```

### ✅ Cách Test ĐÚNG với Mobile App

**Option 1: Test với Real Device (RECOMMENDED)**
- Sử dụng physical device (Android/iOS)
- Lấy FCM token thật từ Firebase SDK
- Gửi notification và verify trên device

**Option 2: Test với Emulator có Google Play Services**
- Android Emulator with Play Store
- iOS Simulator (có hạn chế về push notification)

**Option 3: Test Database & API (Without Real Push)**
- Verify notification saved trong database
- Test các API: list, unread count, mark as read
- Confirm logic hoạt động đúng

---

## 🔧 Test với Postman (Development)

Khi chưa có mobile app hoặc đang development, bạn có thể test các API notification bằng Postman.

### **Setup Postman Collection**

1. Import file `Push_Notification_Tests.postman_collection.json`
2. Tạo Environment với các variables:
   - `base_url`: `http://localhost:8080`
   - `user_token`: Token từ login response
   - `admin_token`: Admin token từ login response
   - `user_id`: User ID từ login response

### **BƯỚC 1: Login để lấy Token**

#### 1.1. Login User
```http
POST {{base_url}}/api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**✅ Expected Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "email": "user@example.com",
    "role": "USER",
    "fullName": "Nguyen Van A"
  }
}
```
💾 **Save:** `user_token` và `user_id`

#### 1.2. Login Admin
```http
POST {{base_url}}/api/auth/login
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "admin123"
}
```

**✅ Expected Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": "4fb95g75-6828-5673-c4gd-3d074g77bgb7",
    "email": "admin@example.com",
    "role": "ADMIN"
  }
}
```
💾 **Save:** `admin_token`

---

### **BƯỚC 2: Test FCM Token Management**

#### 2.1. Register FCM Token (Mock)
```http
PUT {{base_url}}/api/users/{{user_id}}/fcm-token
Authorization: Bearer {{user_token}}
Content-Type: application/json

{
  "fcmToken": "test_fcm_token_from_postman",
  "platform": "android",
  "deviceId": "postman_test_device"
}
```

**✅ Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "FCM token saved successfully",
  "data": null
}
```

**⚠️ Note:** Token mock này chỉ để test API flow, KHÔNG thể nhận push notification thật!

#### 2.2. Verify Token in Database
```sql
SELECT 
    u.email,
    u.fcm_token,
    u.fcm_platform,
    u.fcm_device_id,
    u.fcm_token_updated_at
FROM users u
WHERE u.id = 'your-user-id';
```

**✅ Expected Result:**
| email | fcm_token | fcm_platform | fcm_device_id |
|-------|-----------|--------------|---------------|
| user@example.com | test_fcm_token_from_postman | android | postman_test_device |

---

### **BƯỚC 3: Test Notification History APIs**

#### 3.1. Get All Notifications (Paginated)
```http
GET {{base_url}}/api/users/{{user_id}}/notifications?page=0&size=20
Authorization: Bearer {{user_token}}
```

**✅ Expected Response (200 OK):**
```json
{
  "content": [
    {
      "id": "notif-uuid",
      "title": "Order Confirmed",
      "body": "Your order has been confirmed",
      "type": "ORDER_UPDATE",
      "orderStatus": "CONFIRMED",
      "orderId": "order-uuid",
      "sentAt": "2025-11-03T10:00:00+07:00",
      "readAt": null,
      "isRead": false
    }
  ],
  "totalElements": 10,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

#### 3.2. Get Unread Notifications
```http
GET {{base_url}}/api/users/{{user_id}}/notifications/unread
Authorization: Bearer {{user_token}}
```

**✅ Expected Response (200 OK):**
```json
[
  {
    "id": "notif-uuid-1",
    "title": "Order Ready!",
    "body": "Your order is ready for pickup",
    "type": "ORDER_UPDATE",
    "isRead": false,
    "sentAt": "2025-11-03T11:00:00+07:00"
  }
]
```

#### 3.3. Get Unread Count
```http
GET {{base_url}}/api/users/{{user_id}}/notifications/unread-count
Authorization: Bearer {{user_token}}
```

**✅ Expected Response (200 OK):**
```json
{
  "unreadCount": 3
}
```

#### 3.4. Mark Single Notification as Read
```http
PUT {{base_url}}/api/notifications/{{notification_id}}/read
Authorization: Bearer {{user_token}}
```

**✅ Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Notification marked as read",
  "data": null
}
```

#### 3.5. Mark All Notifications as Read
```http
PUT {{base_url}}/api/users/{{user_id}}/notifications/read-all
Authorization: Bearer {{user_token}}
```

**✅ Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "All notifications marked as read",
  "data": null
}
```

---

### **BƯỚC 4: Test Promotional Notification (Admin)**

#### 4.1. Send to Single User
```http
POST {{base_url}}/api/notifications/promotion
Authorization: Bearer {{admin_token}}
Content-Type: application/json

{
  "userIds": ["{{user_id}}"],
  "title": "🎉 Welcome Promotion",
  "message": "Get 20% OFF on your first order!",
  "imageUrl": "https://example.com/promo.jpg",
  "promoCode": "WELCOME20"
}
```

**✅ Expected Response with Real Token (200 OK):**
```json
{
  "success": true,
  "message": "Promotional notifications sent",
  "data": {
    "total": 1,
    "success": 1,
    "failed": 0,
    "failedUserIds": []
  }
}
```

**⚠️ Expected Response with Mock Token (200 OK):**
```json
{
  "success": true,
  "message": "Promotional notifications sent",
  "data": {
    "total": 1,
    "success": 0,
    "failed": 1,
    "failedUserIds": ["user-id"]
  }
}
```

**🔍 Explanation:**
- ✅ API call successful (200 OK)
- ✅ Notification saved to database
- ❌ Firebase delivery failed (mock token không hợp lệ)

#### 4.2. Verify Notification in Database
```sql
SELECT 
    n.id,
    n.title,
    n.body,
    n.type,
    n.delivery_success,
    n.error_message,
    n.sent_at,
    u.email
FROM notifications n
JOIN users u ON n.user_id = u.id
WHERE n.user_id = 'your-user-id'
ORDER BY n.sent_at DESC
LIMIT 5;
```

**✅ Expected Result:**
| title | type | delivery_success | error_message |
|-------|------|------------------|---------------|
| 🎉 Welcome Promotion | PROMOTION | false | INVALID_ARGUMENT: The registration token is not valid... |

---

### **BƯỚC 5: Test Order Status Notifications**

#### 5.1. Create Test Order
```http
POST {{base_url}}/api/orders
Authorization: Bearer {{user_token}}
Content-Type: application/json

{
  "storeId": "store-uuid",
  "orderItems": [
    {
      "bowlId": "bowl-uuid-1",
      "quantity": 1,
      "price": 50000
    }
  ],
  "totalAmount": 50000,
  "note": "Test order for notification"
}
```

**✅ Save orderId from response**

#### 5.2. Update Order Status (Admin)
```http
POST {{base_url}}/api/orders/{{order_id}}/status
Authorization: Bearer {{admin_token}}
Content-Type: application/json

{
  "status": "CONFIRMED"
}
```

**✅ Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Order status updated successfully",
  "data": {
    "orderId": "order-uuid",
    "status": "CONFIRMED"
  }
}
```

**🔍 Check Notification:**
```sql
SELECT * FROM notifications 
WHERE order_id = 'order-uuid' 
AND order_status = 'CONFIRMED';
```

**✅ Expected Result:**
- Title: "✅ Order Confirmed"
- Body: "Order #ABC12345 confirmed! Estimated time: 30 mins"
- Type: ORDER_UPDATE
- delivery_success: false (with mock token)

#### 5.3. Test Complete Order Flow
```bash
# CONFIRMED
{"status": "CONFIRMED"}
→ Notification: "✅ Order Confirmed"

# PREPARING
{"status": "PREPARING"}
→ Notification: "👨‍🍳 Chef is Cooking"

# READY
{"status": "READY"}
→ Notification: "🎉 Order Ready!"

# COMPLETED
{"status": "COMPLETED"}
→ Notification: "✨ Enjoy Your Meal!"

# CANCELLED
{"status": "CANCELLED"}
→ Notification: "❌ Order Cancelled"
```

---

### **BƯỚC 6: Remove FCM Token (Logout)**
```http
DELETE {{base_url}}/api/users/{{user_id}}/fcm-token
Authorization: Bearer {{user_token}}
```

**✅ Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "FCM token removed successfully",
  "data": null
}
```

---

## 📊 Test Scenarios

### **Scenario 1: Complete Order Flow with Notifications**
1. ✅ User login → Get token & userId
2. ✅ User register FCM token
3. ✅ User create order
4. ✅ Admin update status to CONFIRMED → Notification sent
5. ✅ Admin update status to PREPARING → Notification sent
6. ✅ Admin update status to READY → Notification sent
7. ✅ User check notifications list → See 3 notifications
8. ✅ User check unread count → Count = 3
9. ✅ User mark 1 as read
10. ✅ User check unread count → Count = 2
11. ✅ User mark all as read
12. ✅ User check unread count → Count = 0

### **Scenario 2: Admin Send Bulk Promotions**
1. ✅ Admin login → Get admin token
2. ✅ Prepare list of user IDs (3-5 users)
3. ✅ Admin send promotional notification
4. ✅ Verify response: total, success, failed counts
5. ✅ Check database: All 5 notifications saved
6. ✅ Users login and check notifications
7. ✅ Users receive promotion in notification list

### **Scenario 3: Token Management**
1. ✅ User login on Device 1 (Android) → Register token A
2. ✅ Send notification → User receives on Device 1
3. ✅ User login on Device 2 (iOS) → Register token B
4. ✅ Send notification → User receives on Device 2 ONLY
5. ✅ User logout Device 2 → Token B removed
6. ✅ Send notification → User does NOT receive

---

## 🔍 Kiểm Tra Notification Hoạt Động

### ✅ Checklist để Verify Push Notification

#### 1. Backend Configuration
- [ ] Firebase Admin SDK credentials configured in `application.yml`
- [ ] Firebase project ID correct
- [ ] Service account key file exists and valid
- [ ] Backend server running without errors

#### 2. Mobile App Setup
- [ ] Firebase config file added (google-services.json / GoogleService-Info.plist)
- [ ] Firebase Messaging dependency installed
- [ ] Notification permission requested and granted
- [ ] FCM token successfully retrieved
- [ ] Token sent to backend via API

#### 3. Database Verification
```sql
-- Check user has valid FCM token
SELECT id, email, fcm_token, fcm_platform, fcm_token_updated_at 
FROM users 
WHERE fcm_token IS NOT NULL;

-- Check notification history
SELECT 
    n.title,
    n.type,
    n.order_status,
    n.delivery_success,
    n.error_message,
    n.sent_at,
    u.email
FROM notifications n
JOIN users u ON n.user_id = u.id
ORDER BY n.sent_at DESC
LIMIT 10;

-- Check unread notifications
SELECT COUNT(*) as unread_count
FROM notifications
WHERE user_id = 'your-user-id' AND read_at IS NULL;
```

#### 4. API Testing
- [ ] FCM token registration API works (200 OK)
- [ ] Get notifications list API works
- [ ] Get unread count API works
- [ ] Mark as read API works
- [ ] Send promotional notification API works (Admin)

#### 5. Real Push Testing (with Real Token)
- [ ] User registers real FCM token from mobile app
- [ ] Admin triggers order status change
- [ ] Mobile app receives push notification
- [ ] Notification appears in system tray
- [ ] Clicking notification opens correct screen
- [ ] Notification badge count updates
- [ ] Notification appears in app's notification list

---

## 🐛 Troubleshooting

### Problem 1: Token Saved but Notification Not Received

**Symptoms:**
- API returns 200 OK
- Token saved in database
- Notification saved in database
- But mobile app doesn't receive push

**Possible Causes & Solutions:**

#### A. Invalid or Expired FCM Token
```sql
-- Check token age
SELECT 
    email,
    fcm_token,
    fcm_token_updated_at,
    DATEDIFF(NOW(), fcm_token_updated_at) as days_old
FROM users
WHERE id = 'user-id';
```

**Solution:**
- Refresh token on mobile app
- Re-register token via API
- Implement token refresh listener in app:
```dart
FirebaseMessaging.instance.onTokenRefresh.listen((newToken) {
  // Send new token to server
  updateFCMToken(newToken);
});
```

#### B. Firebase Cloud Messaging Not Enabled
**Solution:**
1. Go to Firebase Console
2. Project Settings → Cloud Messaging
3. Enable Cloud Messaging API
4. Verify Server Key exists

#### C. Wrong Firebase Project
**Solution:**
- Verify `google-services.json` matches backend Firebase project
- Check `mobilesdk_app_id` in config file
- Ensure same project used in backend and mobile app

#### D. Notification Permission Not Granted
**Solution:**
```dart
// Check permission status
NotificationSettings settings = await FirebaseMessaging.instance.getNotificationSettings();
if (settings.authorizationStatus != AuthorizationStatus.authorized) {
  // Request permission again
  await FirebaseMessaging.instance.requestPermission();
}
```

---

### Problem 2: API Returns `success: 0, failed: 1`

**Symptoms:**
```json
{
  "total": 1,
  "success": 0,
  "failed": 1,
  "failedUserIds": ["user-id"]
}
```

**Possible Causes & Solutions:**

#### A. Using Mock/Fake Token
```sql
SELECT fcm_token FROM users WHERE id = 'user-id';
-- Result: test_fcm_token_abc123xyz
```

**Solution:** Use real token from actual mobile device

#### B. Token Unregistered in Firebase
**Backend Logs:**
```
ERROR: Failed to send notification: UNREGISTERED
```

**Solution:**
- User reinstalled app (token invalidated)
- Token expired after 60 days inactivity
- Generate new token from mobile app

#### C. Invalid Argument Error
**Backend Logs:**
```
ERROR: FirebaseMessagingException: INVALID_ARGUMENT
```

**Solution:**
- Token format incorrect
- Token from different Firebase project
- Verify project configuration matches

---

### Problem 3: Notification Received but Not Displayed

**Symptoms:**
- Backend success: true
- Mobile app receives FCM message
- But notification doesn't show in tray

**Solutions:**

#### A. App in Foreground (Flutter)
```dart
// Must handle foreground notifications manually
FirebaseMessaging.onMessage.listen((RemoteMessage message) {
  // Show local notification using flutter_local_notifications
  showLocalNotification(message);
});
```

#### B. Missing Notification Channel (Android 8+)
```dart
const AndroidNotificationChannel channel = AndroidNotificationChannel(
  'order_updates', // id (must match backend)
  'Order Updates', // name
  description: 'Notifications for order status updates',
  importance: Importance.high,
);

await flutterLocalNotificationsPlugin
    .resolvePlatformSpecificImplementation<AndroidFlutterLocalNotificationsPlugin>()
    ?.createNotificationChannel(channel);
```

#### C. Background Restrictions (Battery Optimization)
**Solution:**
- Disable battery optimization for app
- Add app to autostart list
- Check device manufacturer restrictions (Xiaomi, Huawei, etc.)

---

### Problem 4: Database Shows `delivery_success: false`

**Check Error Message:**
```sql
SELECT 
    title,
    error_message,
    sent_at
FROM notifications
WHERE delivery_success = false
ORDER BY sent_at DESC
LIMIT 5;
```

**Common Errors:**

| Error | Meaning | Solution |
|-------|---------|----------|
| `UNREGISTERED` | Token không còn valid | User phải re-register token |
| `INVALID_ARGUMENT` | Token sai format hoặc project khác | Verify Firebase config |
| `SENDER_ID_MISMATCH` | Backend project ≠ App project | Sync Firebase projects |
| `NOT_FOUND` | Token bị xóa | Generate new token |
| `UNAVAILABLE` | Firebase service down | Retry later, check Firebase status |

---

### Problem 5: Notification Click Doesn't Open Correct Screen

**Solution (Flutter):**
```dart
// Handle notification click
void _handleNotificationClick(RemoteMessage message) {
  final type = message.data['type'];
  
  switch (type) {
    case 'order_update':
      final orderId = message.data['orderId'];
      if (orderId != null) {
        Navigator.pushNamed(context, '/order-detail', arguments: orderId);
      }
      break;
    case 'promotion':
      final promoCode = message.data['promoCode'];
      Navigator.pushNamed(context, '/promotions', arguments: promoCode);
      break;
    default:
      Navigator.pushNamed(context, '/notifications');
  }
}

// Register handlers
FirebaseMessaging.onMessageOpenedApp.listen(_handleNotificationClick);

// Check initial message (app opened from terminated state)
RemoteMessage? initialMessage = await FirebaseMessaging.instance.getInitialMessage();
if (initialMessage != null) {
  _handleNotificationClick(initialMessage);
}
```

---

### Problem 6: Multiple Devices Receiving Same Notification

**Expected Behavior:**
- User login on multiple devices
- Each device has different FCM token
- Backend only stores LATEST token
- Only latest device receives notifications

**If Multiple Devices Receive:**

**Check Database:**
```sql
-- Should only have 1 token per user
SELECT id, email, fcm_token, fcm_platform, fcm_device_id
FROM users
WHERE email = 'user@example.com';
```

**Solution:**
- Ensure `PUT /api/users/{userId}/fcm-token` REPLACES old token
- Not creating duplicate tokens
- Backend implementation should be:
```java
user.setFcmToken(fcmToken); // REPLACE, not ADD
user.setFcmTokenUpdatedAt(OffsetDateTime.now());
userRepository.save(user);
```

---

### Debugging Tools

#### 1. Firebase Console Notifications
```
Firebase Console → Cloud Messaging → Send test message
- Enter FCM token
- Send notification directly from Firebase
- Verify token is valid
```

#### 2. Backend Logs
```bash
# Watch backend logs for notification sending
tail -f logs/application.log | grep "notification"

# Look for:
# ✅ "Successfully sent notification to user {userId}"
# ❌ "Failed to send notification: {error}"
```

#### 3. Mobile App Debug Logs
```dart
// Enable Firebase Messaging debug logs
FirebaseMessaging.instance.setAutoInitEnabled(true);

// Log all messages
FirebaseMessaging.onMessage.listen((message) {
  print('🔔 Notification received:');
  print('Title: ${message.notification?.title}');
  print('Body: ${message.notification?.body}');
  print('Data: ${message.data}');
});
```

#### 4. Database Queries
```sql
-- Recent notifications
SELECT * FROM notifications 
WHERE user_id = 'user-id' 
ORDER BY sent_at DESC 
LIMIT 10;

-- Failed notifications
SELECT * FROM notifications 
WHERE delivery_success = false 
ORDER BY sent_at DESC;

-- Notification statistics
SELECT 
    type,
    COUNT(*) as total,
    SUM(CASE WHEN delivery_success THEN 1 ELSE 0 END) as successful,
    SUM(CASE WHEN NOT delivery_success THEN 1 ELSE 0 END) as failed
FROM notifications
GROUP BY type;
```

---

## 📞 Support & Resources

### Documentation
- [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)
- [Flutter Firebase Messaging](https://firebase.flutter.dev/docs/messaging/overview)
- [React Native Firebase](https://rnfirebase.io/)

### Common Issues
- [FCM Troubleshooting Guide](https://firebase.google.com/docs/cloud-messaging/troubleshoot)
- [Android Background Restrictions](https://dontkillmyapp.com/)

### Testing Tools
- [FCM HTTP v1 API Reference](https://firebase.google.com/docs/reference/fcm/rest/v1/projects.messages)
- [Postman Collection for Firebase](https://www.postman.com/google-firebase/)

---

## ✅ Success Criteria

Hệ thống push notification được coi là hoạt động đúng khi:

### Backend
- [x] API save FCM token returns 200 OK
- [x] Token saved correctly in database
- [x] Notification sent when order status changes
- [x] Notification saved to database (even if delivery fails)
- [x] Promotional notification API works
- [x] Notification history APIs return correct data

### Mobile App  
- [x] FCM token successfully retrieved
- [x] Token sent to backend
- [x] App receives push notifications in real-time
- [x] Notifications displayed in system tray
- [x] Notification click opens correct screen
- [x] Badge count updates correctly
- [x] Notification list shows all notifications
- [x] Mark as read functionality works

### End-to-End
- [x] User creates order → Receives notification
- [x] Admin updates order status → User notified immediately
- [x] Admin sends promotion → Users receive
- [x] User logout → Stops receiving notifications
- [x] User login on new device → Receives on new device only

---

## 🎉 Kết Luận

Push notification system của bạn đã sẵn sàng để test với mobile app! 

**Nhớ rằng:**
- ✅ Test với **real FCM token** từ physical device
- ✅ Verify **notification saved** trong database
- ✅ Check **backend logs** để debug
- ✅ Test **all scenarios**: order updates, promotions, logout
- ✅ Verify **notification click** navigation works

Good luck testing! 🚀📱

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

