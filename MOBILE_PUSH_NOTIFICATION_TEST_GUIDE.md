# 📱 Hướng Dẫn Test Push Notification với Mobile App

## 📋 Mục Lục
- [Tổng Quan](#tổng-quan)
- [Chuẩn Bị](#chuẩn-bị)
- [Test với Postman (Backend)](#test-với-postman-backend)
- [Test với Mobile App](#test-với-mobile-app)
- [Kịch Bản Test Chi Tiết](#kịch-bản-test-chi-tiết)
- [Troubleshooting](#troubleshooting)

---

## 🎯 Tổng Quan

Hệ thống Push Notification hỗ trợ:
- ✅ **FCM Token Management**: Đăng ký/xóa token khi login/logout
- ✅ **Order Status Updates**: Tự động gửi thông báo khi đơn hàng thay đổi trạng thái
- ✅ **Promotional Notifications**: Admin gửi thông báo khuyến mãi hàng loạt
- ✅ **Notification History**: Lưu trữ và quản lý lịch sử thông báo
- ✅ **Read/Unread Status**: Theo dõi trạng thái đọc/chưa đọc

---

## 📋 Chuẩn Bị

### 1. Backend Requirements
```bash
✅ Backend đang chạy tại http://localhost:8080
✅ Firebase Admin SDK đã cấu hình trong application.yml
✅ Database đã sẵn sàng
✅ Import file Push_Notification_Tests.postman_collection.json vào Postman
```

### 2. Mobile App Requirements
```bash
✅ Flutter/React Native app với Firebase Messaging
✅ App đã config Firebase (google-services.json / GoogleService-Info.plist)
✅ Request notification permission từ user
✅ Có function để lấy FCM token
```

### 3. Test Accounts
Bạn cần tạo sẵn:
- **1 User Account**: `user@example.com` / `password123`
- **1 Admin Account**: `admin@example.com` / `admin123`
- **1 Order** đang ở trạng thái PENDING hoặc CONFIRMED

---

## 🧪 Test với Postman (Backend)

### **Bước 1: Import Collection**
1. Mở Postman
2. Click **Import** → Chọn file `Push_Notification_Tests.postman_collection.json`
3. Collection sẽ xuất hiện với 5 folder chính

### **Bước 2: Cấu Hình Environment Variables**

#### **Cách 1: Tự động (RECOMMENDED)** ✅
Collection đã được cấu hình **tự động lưu biến môi trường** thông qua **Test Scripts**. Bạn chỉ cần:

1. **Chạy folder "5. Auth" trước tiên:**
   - Gọi `Login as User` → Token và user_id sẽ tự động lưu vào biến
   - Gọi `Login as Admin` → Admin token tự động lưu

2. **Các API khác sẽ tự động sử dụng token đã lưu**

#### **Cách 2: Thủ công**
Nếu muốn set thủ công:
1. Click vào **Collection** → Tab **Variables**
2. Sửa các giá trị:
   ```
   base_url: http://localhost:8080
   user_token: <paste token here>
   admin_token: <paste admin token here>
   user_id: <paste user UUID here>
   ```

### **Bước 3: Chạy Test Flow Cơ Bản**

#### **A. Đăng Ký FCM Token (Login Flow)**

**1. Login as User:**
```
POST {{base_url}}/api/auth/login
Body:
{
  "email": "user@example.com",
  "password": "password123"
}

✅ Response: Token và user_id tự động lưu vào biến
```

**2. Register FCM Token:**
```
PUT {{base_url}}/api/users/{{user_id}}/fcm-token
Headers: Authorization: Bearer {{user_token}}
Body:
{
  "fcmToken": "eXVR...your_real_fcm_token",
  "platform": "android",
  "deviceId": "device_12345"
}

✅ Success: Token đã đăng ký, user sẽ nhận notification
```

> **💡 Lấy Real FCM Token từ đâu?**
> - Chạy mobile app → Khi app request notification permission → Token sẽ được generate
> - Copy token từ app logs hoặc debug console
> - Paste vào `fcmToken` field

#### **B. Test Notification History**

**1. Get All Notifications:**
```
GET {{base_url}}/api/users/{{user_id}}/notifications?page=0&size=20
Headers: Authorization: Bearer {{user_token}}

✅ Response: Danh sách thông báo (paginated)
```

**2. Get Unread Count:**
```
GET {{base_url}}/api/users/{{user_id}}/notifications/unread-count
Headers: Authorization: Bearer {{user_token}}

✅ Response: { "unreadCount": 5 }
```

**3. Mark Notification as Read:**
```
PUT {{base_url}}/api/notifications/{{notification_id}}/read
Headers: Authorization: Bearer {{user_token}}

📝 Note: Lấy notification_id từ response của API Get All Notifications
```

**4. Mark All as Read:**
```
PUT {{base_url}}/api/users/{{user_id}}/notifications/read-all
Headers: Authorization: Bearer {{user_token}}

✅ Success: Tất cả notification đánh dấu đã đọc
```

#### **C. Test Order Status Notifications (Auto-Trigger)**

**1. Login as Admin:**
```
POST {{base_url}}/api/auth/login
Body:
{
  "email": "admin@example.com",
  "password": "admin123"
}

✅ Response: Admin token tự động lưu
```

**2. Update Order Status:**
```
POST {{base_url}}/api/orders/{{order_id}}/status
Headers: Authorization: Bearer {{admin_token}}
Body: { "status": "CONFIRMED" }

🔔 Notification sẽ TỰ ĐỘNG gửi đến mobile app của user!
```

**Các trạng thái khác:**
- `CONFIRMED` → "✅ Order Confirmed! We're preparing your healthy meal."
- `PREPARING` → "👨‍🍳 Chef is Cooking! Your order is being prepared with care."
- `READY` → "🎉 Order Ready! Your delicious meal is ready for pickup."
- `COMPLETED` → "😋 Enjoy Your Meal! Thank you for choosing us."
- `CANCELLED` → "❌ Order Cancelled. Please contact support if you need help."

#### **D. Test Promotional Notifications (Admin)**

**1. Send to Single User:**
```
POST {{base_url}}/api/notifications/promotion
Headers: Authorization: Bearer {{admin_token}}
Body:
{
  "userIds": ["{{user_id}}"],
  "title": "🎉 Welcome Bonus!",
  "message": "Get 20% off on your first order. Use code: WELCOME20",
  "imageUrl": "https://example.com/promo.jpg",
  "promoCode": "WELCOME20"
}

✅ User sẽ nhận notification ngay lập tức
```

**2. Send to Multiple Users:**
```
POST {{base_url}}/api/notifications/promotion
Headers: Authorization: Bearer {{admin_token}}
Body:
{
  "userIds": ["uuid-1", "uuid-2", "uuid-3"],
  "title": "🔥 Flash Sale 50% OFF!",
  "message": "Limited time! Get 50% discount on all healthy bowls.",
  "imageUrl": "https://example.com/flash-sale.jpg",
  "promoCode": "FLASH50"
}

✅ Response:
{
  "total": 3,
  "success": 2,
  "failed": 1,
  "failedUserIds": ["uuid-3"]
}
```

---

## 📱 Test với Mobile App

### **Bước 1: Setup Firebase trong Mobile App**

#### **Flutter Example:**

**1. Install Dependencies (pubspec.yaml):**
```yaml
dependencies:
  firebase_core: ^2.24.0
  firebase_messaging: ^14.7.6
  flutter_local_notifications: ^16.3.0
```

**2. Initialize Firebase (main.dart):**
```dart
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_messaging/firebase_messaging.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp();
  
  // Handle background messages
  FirebaseMessaging.onBackgroundMessage(_firebaseMessagingBackgroundHandler);
  
  runApp(MyApp());
}

Future<void> _firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  print("Background message: ${message.notification?.title}");
}
```

**3. Request Permission & Get FCM Token:**
```dart
class NotificationService {
  final FirebaseMessaging _messaging = FirebaseMessaging.instance;

  Future<void> init() async {
    // Request permission (iOS)
    NotificationSettings settings = await _messaging.requestPermission(
      alert: true,
      badge: true,
      sound: true,
    );

    if (settings.authorizationStatus == AuthorizationStatus.authorized) {
      print('✅ User granted permission');
      
      // Get FCM token
      String? token = await _messaging.getToken();
      print('📱 FCM Token: $token');
      
      // Send token to backend
      await _registerTokenToBackend(token);
    }
  }

  Future<void> _registerTokenToBackend(String? token) async {
    if (token == null) return;
    
    final response = await http.put(
      Uri.parse('http://YOUR_API/api/users/$userId/fcm-token'),
      headers: {
        'Authorization': 'Bearer $accessToken',
        'Content-Type': 'application/json',
      },
      body: jsonEncode({
        'fcmToken': token,
        'platform': Platform.isAndroid ? 'android' : 'ios',
        'deviceId': 'device_${Random().nextInt(10000)}',
      }),
    );
    
    print('✅ Token registered: ${response.statusCode}');
  }
}
```

**4. Listen to Foreground Messages:**
```dart
void setupForegroundMessageListener() {
  FirebaseMessaging.onMessage.listen((RemoteMessage message) {
    print('📩 Foreground message received:');
    print('Title: ${message.notification?.title}');
    print('Body: ${message.notification?.body}');
    print('Data: ${message.data}');
    
    // Show local notification
    _showLocalNotification(message);
  });
}

void _showLocalNotification(RemoteMessage message) {
  // Use flutter_local_notifications to show notification
  // when app is in foreground
}
```

**5. Handle Notification Tap:**
```dart
void setupNotificationTapHandler() {
  // App opened from terminated state
  FirebaseMessaging.instance.getInitialMessage().then((message) {
    if (message != null) {
      _handleNotificationTap(message.data);
    }
  });

  // App opened from background
  FirebaseMessaging.onMessageOpenedApp.listen((message) {
    _handleNotificationTap(message.data);
  });
}

void _handleNotificationTap(Map<String, dynamic> data) {
  String type = data['type'] ?? '';
  
  if (type == 'order_update') {
    String orderId = data['orderId'] ?? '';
    // Navigate to order detail page
    Navigator.push(context, OrderDetailPage(orderId: orderId));
  } else if (type == 'promotion') {
    String promoCode = data['promoCode'] ?? '';
    // Navigate to promo page or show promo dialog
  }
}
```

#### **React Native Example:**

**1. Install Dependencies:**
```bash
npm install @react-native-firebase/app
npm install @react-native-firebase/messaging
```

**2. Request Permission & Get Token:**
```javascript
import messaging from '@react-native-firebase/messaging';

async function requestUserPermission() {
  const authStatus = await messaging().requestPermission();
  const enabled =
    authStatus === messaging.AuthorizationStatus.AUTHORIZED ||
    authStatus === messaging.AuthorizationStatus.PROVISIONAL;

  if (enabled) {
    console.log('✅ Authorization status:', authStatus);
    const token = await messaging().getToken();
    console.log('📱 FCM Token:', token);
    
    // Send to backend
    await registerTokenToBackend(token);
  }
}

async function registerTokenToBackend(token) {
  await fetch(`${API_URL}/api/users/${userId}/fcm-token`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      fcmToken: token,
      platform: Platform.OS,
      deviceId: `device_${Math.random()}`,
    }),
  });
}
```

**3. Listen to Foreground Messages:**
```javascript
useEffect(() => {
  const unsubscribe = messaging().onMessage(async remoteMessage => {
    console.log('📩 Foreground message:', remoteMessage);
    Alert.alert(
      remoteMessage.notification?.title || 'Notification',
      remoteMessage.notification?.body || ''
    );
  });

  return unsubscribe;
}, []);
```

### **Bước 2: Test Flow Hoàn Chỉnh**

#### **Test 1: Login Flow**
```
1. Mở mobile app
2. Login với user@example.com
3. App tự động:
   - Request notification permission
   - Get FCM token
   - Call API: PUT /api/users/{userId}/fcm-token
4. ✅ Check logs: "Token registered: 200"
```

#### **Test 2: Receive Order Notification**
```
1. Trong Postman, login as Admin
2. Tạo hoặc chọn 1 order của user
3. Call API: POST /api/orders/{orderId}/status
   Body: { "status": "CONFIRMED" }
4. ✅ Mobile app sẽ nhận notification ngay lập tức!
5. Tap vào notification → App mở và navigate đến Order Detail
```

#### **Test 3: Receive Promotion Notification**
```
1. Trong Postman, login as Admin
2. Call API: POST /api/notifications/promotion
   Body:
   {
     "userIds": ["<user_id>"],
     "title": "🎉 Special Offer",
     "message": "Get 30% off today!",
     "promoCode": "SAVE30"
   }
3. ✅ Mobile app nhận notification với promo code
4. Tap vào notification → Show promo dialog với code
```

#### **Test 4: Notification History**
```
1. Trong app, mở Notification Screen
2. Call API: GET /api/users/{userId}/notifications
3. ✅ Hiển thị list notifications với:
   - Title, body, timestamp
   - Read/unread status (badge hoặc bold text)
   - Unread count badge trên icon
4. Tap vào 1 notification:
   - Call API: PUT /api/notifications/{notificationId}/read
   - Navigate to detail page
5. Check API: GET /api/users/{userId}/notifications/unread-count
   ✅ Count giảm đi 1
```

#### **Test 5: Logout Flow**
```
1. User logout trong app
2. App call API: DELETE /api/users/{userId}/fcm-token
3. ✅ Token bị xóa khỏi database
4. Update order status → User KHÔNG nhận notification nữa
5. Login lại → Token được đăng ký lại → Nhận notification bình thường
```

---

## 🎯 Kịch Bản Test Chi Tiết

### **Scenario 1: User Order Flow (End-to-End)**

```
📱 USER APP                  🖥️ BACKEND              🔔 NOTIFICATION

1. User login
   → Get FCM token           → Save token           
   → Register token          → Success ✅

2. User tạo order
   → POST /api/orders        → Order created
                              → Status: PENDING

3. Admin confirm order
   → (Postman/Admin panel)   → Update status         → 🔔 "Order Confirmed"
                              → CONFIRMED            → Mobile nhận notification

4. Kitchen staff preparing
   → Update status           → PREPARING            → 🔔 "Chef is Cooking"
                                                     → Mobile nhận notification

5. Order ready
   → Update status           → READY                → 🔔 "Order Ready!"
                                                     → Mobile nhận notification

6. User pickup
   → Update status           → COMPLETED            → 🔔 "Enjoy Your Meal!"
                                                     → Mobile nhận notification

7. User check history
   → GET /notifications      → Return 4 notifs      
   → Display in app          → All with correct     
                                timestamps & status
```

### **Scenario 2: Promotion Campaign**

```
🖥️ ADMIN                      🖥️ BACKEND              📱 USERS

1. Admin plan campaign
   → Get list of active       → Query users
      users

2. Send bulk promotion
   → POST /notifications/     → Process list         → 🔔 500 users receive
      promotion                 of 500 users           "Flash Sale" notification
   → userIds: [500 UUIDs]    
                              → Success: 485
                              → Failed: 15
                              
3. Users tap notification
   → Navigate to promo page   → Log analytics
   → Apply promo code         → Track conversion
   → Place order with         → Order with discount
      discount
```

### **Scenario 3: Error Handling**

```
TEST CASE: Invalid FCM Token

1. User uninstall app (token invalid)
2. Admin update order status
3. Backend try send notification
4. ❌ FCM returns: UNREGISTERED error
5. ✅ Backend auto remove invalid token
6. ✅ Notification saved with error log
7. User reinstall app → Register new token → Work normally
```

---

## 🔍 Kiểm Tra Notification Có Hoạt Động

### **Checklist:**

#### **Backend:**
```
✅ Firebase credentials trong application.yml đúng
✅ API /api/users/{userId}/fcm-token trả về 200
✅ Database có record FCM token mới (check users table)
✅ Logs hiển thị: "Successfully sent notification to user..."
✅ Database notification table có record mới
```

#### **Mobile App:**
```
✅ Firebase config files đúng vị trí (google-services.json / GoogleService-Info.plist)
✅ Notification permission = AUTHORIZED
✅ FCM token không null (check logs)
✅ API register token trả về 200
✅ Foreground listener đang hoạt động
✅ Background handler đã setup
✅ Notification tap handler hoạt động
```

#### **Test thử:**
```bash
# Test 1: Send test notification from Firebase Console
1. Mở Firebase Console
2. Cloud Messaging → New notification
3. Paste FCM token
4. Send test message
✅ App nhận được → Firebase config OK

# Test 2: Send from Backend API
1. Postman: Update order status
2. Check backend logs: "Successfully sent..."
3. Check mobile logs: "Foreground message received..."
✅ App nhận được → Backend integration OK
```

---

## ❗ Troubleshooting

### **Problem 1: Mobile app không nhận notification**

#### **Check 1: FCM Token**
```dart
// In app, print token
String? token = await FirebaseMessaging.instance.getToken();
print('Token: $token');

❌ Token = null:
  → Check Firebase config files
  → Check google-services.json trong android/app/
  → Check GoogleService-Info.plist trong ios/Runner/
  → Rebuild app

✅ Token có giá trị:
  → Copy token
  → Test trực tiếp từ Firebase Console
```

#### **Check 2: Permission**
```dart
NotificationSettings settings = await FirebaseMessaging.instance.requestPermission();
print('Status: ${settings.authorizationStatus}');

❌ Status = denied:
  → Uninstall app
  → Reinstall
  → Grant permission lại

❌ Status = notDetermined:
  → Call requestPermission() again
```

#### **Check 3: Backend Token Registration**
```bash
# Check database
SELECT fcm_token, fcm_platform, fcm_token_updated_at 
FROM users 
WHERE id = 'your-user-id';

❌ fcm_token = NULL:
  → App chưa call API register token
  → Check network request trong app logs
  → Check API response (có thể 401 Unauthorized)

✅ fcm_token có giá trị:
  → Backend đã lưu token thành công
```

#### **Check 4: Notification Sending**
```bash
# Check backend logs khi update order status
✅ Log: "Successfully sent notification to user..."
  → Backend đã gửi thành công
  → Vấn đề có thể ở mobile app

❌ Log: "Failed to send notification: UNREGISTERED"
  → Token không hợp lệ
  → User có thể đã uninstall app
  → Clear token và đăng ký lại

❌ Log: "User has no FCM token"
  → User chưa đăng ký token
  → Check bước register token
```

### **Problem 2: Notification đến nhưng không hiển thị**

#### **Android:**
```kotlin
// Check notification channel
// In MainActivity.kt or NotificationService
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    val channel = NotificationChannel(
        "order_updates",
        "Order Updates",
        NotificationManager.IMPORTANCE_HIGH
    )
    notificationManager.createNotificationChannel(channel)
}

// Hoặc trong Flutter:
const AndroidNotificationChannel channel = AndroidNotificationChannel(
  'order_updates',
  'Order Updates',
  importance: Importance.high,
);
await flutterLocalNotificationsPlugin
    .resolvePlatformSpecificImplementation<AndroidFlutterLocalNotificationsPlugin>()
    ?.createNotificationChannel(channel);
```

#### **iOS:**
```bash
# Check Info.plist có UIBackgroundModes
<key>UIBackgroundModes</key>
<array>
    <string>remote-notification</string>
</array>

# Check permission trong Settings app
Settings > Your App > Notifications > Allow Notifications = ON
```

### **Problem 3: Background notification không hoạt động**

#### **Flutter:**
```dart
// Đảm bảo có background handler
@pragma('vm:entry-point')
Future<void> _firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  await Firebase.initializeApp();
  print("Background message: ${message.notification?.title}");
}

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp();
  
  // MUST register BEFORE runApp()
  FirebaseMessaging.onBackgroundMessage(_firebaseMessagingBackgroundHandler);
  
  runApp(MyApp());
}
```

#### **React Native:**
```javascript
// index.js (NOT App.js)
import messaging from '@react-native-firebase/messaging';

messaging().setBackgroundMessageHandler(async remoteMessage => {
  console.log('Background message:', remoteMessage);
});

AppRegistry.registerComponent(appName, () => App);
```

### **Problem 4: Notification tap không navigate**

```dart
// Đảm bảo có setup handler
FirebaseMessaging.onMessageOpenedApp.listen((RemoteMessage message) {
  print('Notification tapped: ${message.data}');
  
  // MUST use Navigator with context
  WidgetsBinding.instance.addPostFrameCallback((_) {
    Navigator.pushNamed(context, '/order-detail', arguments: message.data);
  });
});

// Check initial message (app was terminated)
FirebaseMessaging.instance.getInitialMessage().then((message) {
  if (message != null) {
    print('App opened from terminated state');
    // Navigate after app fully loaded
    Future.delayed(Duration(seconds: 2), () {
      Navigator.pushNamed(context, '/order-detail', arguments: message.data);
    });
  }
});
```

### **Problem 5: Postman không tự động lưu biến**

#### **Check Test Scripts:**
```javascript
// Trong Login API, tab "Tests" phải có:
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    if (jsonData.data && jsonData.data.accessToken) {
        pm.collectionVariables.set('user_token', jsonData.data.accessToken);
        pm.collectionVariables.set('user_id', jsonData.data.userId);
        console.log('✅ Token saved');
    }
}

// Check console output sau khi gọi API
// View > Show Postman Console
// Phải thấy: "✅ Token saved"
```

#### **Sửa lỗi:**
```
1. Click vào request "Login as User"
2. Tab "Tests"
3. Copy script từ collection JSON
4. Save request
5. Gọi lại API
6. Check Collection Variables: Click vào Collection > Variables
   → user_token phải có giá trị
```

---

## 🎓 Best Practices

### **1. Token Management**
```
✅ Register token NGAY SAU KHI login thành công
✅ Remove token KHI logout
✅ Refresh token khi app restart (token có thể thay đổi)
✅ Handle token refresh event:
   FirebaseMessaging.instance.onTokenRefresh.listen((newToken) {
     registerTokenToBackend(newToken);
   });
```

### **2. Error Handling**
```
✅ Show error message nếu register token fail
✅ Retry logic cho network errors
✅ Log all notification events để debug
✅ Handle expired/invalid tokens gracefully
```

### **3. User Experience**
```
✅ Request permission tại thời điểm phù hợp (không phải ngay khi mở app)
✅ Explain tại sao cần notification permission
✅ Show badge count cho unread notifications
✅ Group notifications by type
✅ Clear notifications khi user view detail
```

### **4. Testing**
```
✅ Test trên cả Android và iOS
✅ Test trên cả real device và emulator
✅ Test app ở 3 states: foreground, background, terminated
✅ Test với network slow/offline
✅ Test với nhiều notifications cùng lúc
```

---

## 📊 Testing Checklist

### **Phase 1: Setup** ✅
- [ ] Backend running và Firebase configured
- [ ] Mobile app có Firebase dependencies
- [ ] Firebase config files đúng vị trí
- [ ] Postman collection imported
- [ ] Test accounts created

### **Phase 2: Basic Flow** ✅
- [ ] User login → Token registered successfully
- [ ] Backend có token trong database
- [ ] Send test notification từ Firebase Console → App nhận được
- [ ] Send notification từ Postman → App nhận được

### **Phase 3: Order Flow** ✅
- [ ] Update order to CONFIRMED → Notification received
- [ ] Update order to PREPARING → Notification received
- [ ] Update order to READY → Notification received
- [ ] Update order to COMPLETED → Notification received
- [ ] Update order to CANCELLED → Notification received

### **Phase 4: Notification History** ✅
- [ ] Get all notifications → Display list
- [ ] Get unread count → Show badge
- [ ] Mark as read → Badge updated
- [ ] Mark all as read → All badges cleared

### **Phase 5: Promotional** ✅
- [ ] Admin send promo to 1 user → User received
- [ ] Admin send promo to multiple users → All received
- [ ] Tap notification → Navigate to promo page
- [ ] Promo code extracted from notification data

### **Phase 6: Edge Cases** ✅
- [ ] User logout → Token removed → No notification
- [ ] User login again → Token re-registered → Receive notification
- [ ] Invalid token → Backend auto clear token
- [ ] App in foreground → Notification displayed
- [ ] App in background → Notification displayed
- [ ] App terminated → Notification displayed
- [ ] Tap notification → App opened and navigated correctly

---

## 🔗 Resources

- **Firebase Console**: https://console.firebase.google.com
- **FCM Documentation**: https://firebase.google.com/docs/cloud-messaging
- **Flutter Firebase Messaging**: https://pub.dev/packages/firebase_messaging
- **React Native Firebase**: https://rnfirebase.io

---

## 📞 Support

Nếu gặp vấn đề:
1. Check logs cả backend và mobile app
2. Check Firebase Console → Cloud Messaging
3. Test với Firebase Console trước khi test với backend
4. Check Postman Console để debug biến môi trường

**Contact**: [Your Email/Slack]

---

**Last Updated**: November 2025
**Version**: 2.0

