# 🚀 Push Notification Implementation Requirements

## 📋 Overview
Implement Firebase Cloud Messaging (FCM) push notifications for the Healthy Food Ordering app with complete order lifecycle notifications.

## 🎯 Must-Have Features (Priority 1)

### 1. FCM Token Management APIs

#### 1.1 Save FCM Token
```http
PUT /api/users/{userId}/fcm-token
```

**Request Body:**
```json
{
  "fcmToken": "dXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX...",
  "platform": "android",
  "deviceId": "unique_device_id"
}
```

**Response:**
```json
{
  "success": true,
  "message": "FCM token saved successfully"
}
```

#### 1.2 Remove FCM Token (Logout)
```http
DELETE /api/users/{userId}/fcm-token
```

**Response:**
```json
{
  "success": true,
  "message": "FCM token removed successfully"
}
```

### 2. Order Status Notification APIs

#### 2.1 Create Order with Notification
```http
POST /api/orders
```

**Request Body:**
```json
{
  "userId": "user_id",
  "items": [...],
  "totalAmount": 25.99,
  "deliveryAddress": {...}
}
```

**Auto-send Notification:**
- Title: "🔔 Order Received"
- Body: "Your order #12345 has been received. Total: $25.99"

#### 2.2 Update Order Status with Notification
```http
PUT /api/orders/{orderId}/status
```

**Request Body:**
```json
{
  "status": "confirmed"
}
```

**Auto-send Notifications based on status:**
- `confirmed` → "✅ Order Confirmed - Estimated time: 30 mins"
- `preparing` → "👨‍🍳 Chef is preparing your delicious meal!"
- `ready` → "🎉 Order Ready for Pickup at Restaurant Name"
- `out_for_delivery` → "🚚 Your order is on the way!"
- `delivered` → "✨ Enjoy your meal! Don't forget to rate your experience"
- `cancelled` → "❌ Order cancelled. Refund will be processed within 3-5 days"

### 3. Database Schema Updates

#### 3.1 User Table - Add FCM Fields
```sql
ALTER TABLE users
ADD COLUMN fcm_token TEXT,
ADD COLUMN fcm_platform VARCHAR(20),
ADD COLUMN fcm_device_id VARCHAR(255),
ADD COLUMN fcm_token_updated_at TIMESTAMP;
```

#### 3.2 Notification History Table (Optional)
```sql
CREATE TABLE notifications (
  id UUID PRIMARY KEY,
  user_id UUID REFERENCES users(id),
  order_id UUID REFERENCES orders(id),
  title VARCHAR(255),
  body TEXT,
  type VARCHAR(50), -- 'order_update', 'promotion', etc.
  status VARCHAR(50), -- order status
  sent_at TIMESTAMP,
  read_at TIMESTAMP
);
```

## 🎨 Nice-to-Have Features (Priority 2)

### 4. Promotional Notifications API
```http
POST /api/notifications/promotion
```

**Request Body:**
```json
{
  "userIds": ["user1", "user2"],
  "title": "Flash Sale!",
  "message": "50% off on all bowls today!",
  "imageUrl": "https://...",
  "promoCode": "FLASH50"
}
```

### 5. Scheduled Notifications (Cron Jobs)
- Lunch reminders at 11:30 AM
- Abandoned cart reminders (after 1 hour)
- Special promotions on weekends

## 🔧 Technical Requirements

### 1. Firebase Admin SDK Setup
```javascript
const admin = require('firebase-admin');
const serviceAccount = require('./service-account-key.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});
```

### 2. FCM Message Structure
```javascript
const message = {
  token: user.fcmToken,
  notification: {
    title: "Order Update",
    body: "Your order is ready!"
  },
  data: {
    type: "order_update",
    orderId: "123",
    status: "ready",
    click_action: "FLUTTER_NOTIFICATION_CLICK"
  },
  android: {
    notification: {
      channelId: "order_updates",
      priority: "high",
      color: "#52946B"
    }
  }
};
```

### 3. Error Handling
- Handle invalid/expired FCM tokens
- Retry failed notifications
- Log notification delivery status

## 📱 Mobile App Requirements (Already Done)

### 1. FCM Token Management
- Auto-generate FCM token on app start
- Send to backend on login
- Remove from backend on logout

### 2. Notification Handling
- Foreground notifications → Show local notification
- Background notifications → Show in tray
- App opened from notification → Navigate to relevant screen

## 🧪 Testing Checklist

### 1. FCM Token Flow
- [ ] Login → FCM token saved to backend
- [ ] Logout → FCM token removed from backend
- [ ] Multiple devices → Each device has separate token

### 2. Order Notifications
- [ ] Order placed → "Order received" notification
- [ ] Status changes → Appropriate notifications
- [ ] Deep linking → Click notification → Go to order details

### 3. Edge Cases
- [ ] User without FCM token → No error
- [ ] Invalid FCM token → Handle gracefully
- [ ] Network issues → Retry mechanism

## 📊 Success Metrics

- **Delivery Rate:** >95% notifications delivered
- **Open Rate:** >30% notifications clicked
- **User Engagement:** Increased order frequency
- **Retention:** Better user retention through timely updates

## 🚀 Implementation Priority

1. **Day 1:** FCM token management APIs
2. **Day 2:** Order status notification integration
3. **Day 3:** Testing and error handling
4. **Day 4:** Promotional notifications (if time)

---

**📞 Contact:** If you need clarification on any endpoint or message structure, please ask!

**🔗 Resources:**
- [Firebase Admin SDK Docs](https://firebase.google.com/docs/admin/setup)
- [FCM HTTP v1 API](https://firebase.google.com/docs/cloud-messaging/http-server-ref)
- [Flutter FCM Plugin](https://pub.dev/packages/firebase_messaging)
