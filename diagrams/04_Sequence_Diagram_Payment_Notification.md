# Sequence Diagrams - Payment & Notification Flows

## 1. ZaloPay Payment Flow

```plantuml
@startuml
title ZaloPay Payment Flow

actor Customer
participant "Mobile App" as App
participant "ZaloPayController" as Controller
participant "ZaloPayService" as ZaloService
participant "OrderService" as OrderService
participant "PaymentTransactionService" as PaymentService
participant "ZaloPay Gateway" as ZaloPay
participant "Database" as DB

== Initiate Payment ==
Customer -> App: Click "Pay with ZaloPay"
App -> Controller: POST /api/zalopay/create-order\n{orderId, amount}
activate Controller

Controller -> ZaloService: createOrder(orderId, amount)
activate ZaloService

ZaloService -> OrderService: findById(orderId)
activate OrderService
OrderService -> DB: SELECT * FROM orders WHERE id=?
DB --> OrderService: Order
deactivate OrderService

' Create payment transaction record
ZaloService -> PaymentService: create(PaymentTransaction)
activate PaymentService
PaymentService -> DB: INSERT INTO payment_transactions\n(orderId, amount, method=ZALOPAY,\nstatus=PENDING, transactionId)
DB --> PaymentService: Transaction created
deactivate PaymentService

' Prepare ZaloPay request
ZaloService -> ZaloService: Build ZaloPay request
note right
  {
    app_id: "2553",
    app_trans_id: "YYMMDD_orderId",
    app_user: "userId",
    amount: totalAmount,
    item: JSON of order items,
    embed_data: {orderId, ...},
    callback_url: "https://api.../zalopay/callback",
    mac: HMAC-SHA256 signature
  }
end note

ZaloService -> ZaloPay: POST /createorder
activate ZaloPay
ZaloPay -> ZaloPay: Validate request
ZaloPay -> ZaloPay: Generate payment URL
ZaloPay --> ZaloService: {return_code: 1,\norder_url, zp_trans_token}
deactivate ZaloPay

ZaloService --> Controller: {orderUrl, zpTransToken}
deactivate ZaloService
Controller --> App: 200 OK\n{paymentUrl}
deactivate Controller

== Customer Completes Payment ==
App -> App: Open ZaloPay payment URL
Customer -> "ZaloPay App": Authenticate & confirm payment
"ZaloPay App" --> Customer: Payment successful

== ZaloPay Callback ==
ZaloPay -> Controller: POST /api/zalopay/callback\n{data, mac, type}
activate Controller

Controller -> ZaloService: handleCallback(callbackData)
activate ZaloService

' Verify callback signature
ZaloService -> ZaloService: verifyMac(data, mac)
alt Invalid MAC
    ZaloService --> Controller: {return_code: -1,\nreturn_message: "Invalid MAC"}
    Controller --> ZaloPay: Failed
else Valid MAC
    ZaloService -> ZaloService: Parse callback data
    note right
      {
        app_trans_id,
        zp_trans_id,
        status: 1 (success) or 2 (failed),
        amount,
        server_time,
        ...
      }
    end note
    
    ' Update payment transaction
    ZaloService -> PaymentService: updateByTransactionId(app_trans_id, status)
    activate PaymentService
    PaymentService -> DB: UPDATE payment_transactions SET\nstatus='COMPLETED',\nzpTransId=?, completedAt=NOW()\nWHERE transactionId=?
    DB --> PaymentService: Updated
    deactivate PaymentService
    
    ' Update order payment status
    ZaloService -> OrderService: updatePaymentStatus(orderId, PAID)
    activate OrderService
    OrderService -> DB: UPDATE orders SET\npaymentStatus='PAID'\nWHERE id=?
    deactivate OrderService
    
    ZaloService --> Controller: {return_code: 1,\nreturn_message: "Success"}
    deactivate ZaloService
    Controller --> ZaloPay: Success response
end
deactivate Controller

== Query Payment Status ==
App -> Controller: GET /api/zalopay/query-status/{orderId}
activate Controller

Controller -> ZaloService: queryOrderStatus(orderId)
activate ZaloService

ZaloService -> PaymentService: findByOrderId(orderId)
activate PaymentService
PaymentService -> DB: SELECT * FROM payment_transactions\nWHERE orderId=? ORDER BY createdAt DESC
DB --> PaymentService: Transaction
deactivate PaymentService

ZaloService --> Controller: {status, amount, zpTransId}
deactivate ZaloService
Controller --> App: 200 OK\n{paymentStatus, transaction}
deactivate Controller

App --> Customer: Show payment result

@enduml
```

## 2. Push Notification Flow (FCM)

```plantuml
@startuml
title Push Notification Flow - Order Status Update

actor "Kitchen Staff" as Staff
participant "OrderController" as Controller
participant "OrderService" as OrderService
participant "FcmService" as FcmService
participant "NotificationService" as NotificationService
participant "Firebase Admin SDK" as FirebaseSDK
participant "Firebase FCM Server" as FCM
participant "Customer Mobile" as Mobile
participant "Database" as DB

== Order Status Update with Notification ==
Staff -> Controller: PUT /api/orders/{orderId}/status\n{status: "PREPARING"}
activate Controller

Controller -> OrderService: update(orderId, status)
activate OrderService
OrderService -> DB: UPDATE orders SET status='PREPARING'
DB --> OrderService: Order updated
deactivate OrderService

Controller -> FcmService: sendOrderNotification(order, PREPARING)
activate FcmService

' Get user's FCM token
FcmService -> DB: SELECT u.fcmToken, u.fullName\nFROM users u\nJOIN orders o ON o.userId=u.id\nWHERE o.id=?
DB --> FcmService: {fcmToken, fullName}

alt FCM Token not found
    FcmService --> Controller: Warning: No FCM token
    note right: Log warning but don't fail request
else FCM Token exists
    
    ' Build notification message
    FcmService -> FcmService: buildNotificationMessage(status)
    note right
      {
        token: user.fcmToken,
        notification: {
          title: "👨‍🍳 Order Update",
          body: "Chef is preparing your delicious meal!",
          image: storeImageUrl
        },
        data: {
          type: "order_update",
          orderId: "uuid",
          orderStatus: "PREPARING",
          click_action: "FLUTTER_NOTIFICATION_CLICK"
        },
        android: {
          priority: "high",
          notification: {
            channelId: "order_updates",
            color: "#52946B",
            sound: "default"
          }
        },
        apns: {
          payload: {
            aps: {
              sound: "default",
              badge: 1
            }
          }
        }
      }
    end note
    
    ' Send via Firebase
    FcmService -> FirebaseSDK: send(message)
    activate FirebaseSDK
    FirebaseSDK -> FCM: POST /v1/projects/.../messages:send
    activate FCM
    
    FCM -> Mobile: Push notification
    activate Mobile
    
    alt App in Foreground
        Mobile -> Mobile: onMessage() - Show local notification
    else App in Background
        Mobile -> Mobile: onBackgroundMessage() - System notification
    else App Terminated
        Mobile -> Mobile: System shows notification in tray
    end
    
    FCM --> FirebaseSDK: {name: "projects/.../messages/0:..."}
    deactivate FCM
    FirebaseSDK --> FcmService: Message ID
    deactivate FirebaseSDK
    
    deactivate Mobile
    
    ' Save notification to history
    FcmService -> NotificationService: saveNotification(notification)
    activate NotificationService
    NotificationService -> DB: INSERT INTO notifications\n(userId, orderId, title, body,\ntype=ORDER_UPDATE, orderStatus,\nsentAt=NOW())
    DB --> NotificationService: Notification saved
    deactivate NotificationService
    
    FcmService --> Controller: Notification sent successfully
end
deactivate FcmService

Controller --> Staff: 200 OK\n{order updated}
deactivate Controller

== User Opens Notification ==
Customer -> Mobile: Tap notification
Mobile -> Mobile: getInitialMessage() or onMessageOpenedApp()
Mobile -> Mobile: Navigate to OrderDetailScreen(orderId)
Mobile --> Customer: Show order details

@enduml
```

## 3. Promotional Notification Flow

```plantuml
@startuml
title Send Promotional Notification to Multiple Users

actor Admin
participant "NotificationController" as Controller
participant "FcmService" as FcmService
participant "NotificationService" as NotificationService
participant "UserService" as UserService
participant "Firebase FCM" as FCM
participant "Database" as DB

Admin -> Controller: POST /api/notifications/promotion\n{userIds[], title, message,\nimageUrl, promoCode}
activate Controller

Controller -> FcmService: sendPromotionalNotification(request)
activate FcmService

' Get users with FCM tokens
FcmService -> UserService: findUsersByIds(userIds)
activate UserService
UserService -> DB: SELECT id, fcmToken, fullName\nFROM users\nWHERE id IN (?) AND fcmToken IS NOT NULL
DB --> UserService: List<User> with FCM tokens
deactivate UserService

' Send to each user
loop For each user
    FcmService -> FcmService: buildPromotionalMessage(user, promo)
    note right
      {
        token: user.fcmToken,
        notification: {
          title: "🎉 Flash Sale!",
          body: "50% off on all bowls today!",
          image: imageUrl
        },
        data: {
          type: "promotion",
          promoCode: "FLASH50",
          imageUrl: "...",
          click_action: "FLUTTER_NOTIFICATION_CLICK"
        },
        android: {
          priority: "high",
          notification: {
            channelId: "promotions",
            color: "#52946B"
          }
        }
      }
    end note
    
    FcmService -> FCM: send(message)
    activate FCM
    FCM --> FcmService: Message sent
    deactivate FCM
    
    ' Save notification history
    FcmService -> NotificationService: saveNotification(userId, promo)
    activate NotificationService
    NotificationService -> DB: INSERT INTO notifications\n(userId, title, body,\ntype=PROMOTION, sentAt)
    deactivate NotificationService
end

FcmService --> Controller: {successCount, failureCount}
deactivate FcmService

Controller --> Admin: 200 OK\n{sent: 150, failed: 5}
deactivate Controller

@enduml
```

## 4. View Notification History Flow

```plantuml
@startuml
title View User Notification History

actor Customer
participant "NotificationController" as Controller
participant "NotificationService" as NotificationService
participant "Database" as DB

== Get All Notifications ==
Customer -> Controller: GET /api/users/{userId}/notifications\n?page=0&size=20
activate Controller

Controller -> NotificationService: getUserNotifications(userId, pageable)
activate NotificationService

NotificationService -> DB: SELECT n.*, o.totalAmount, o.status\nFROM notifications n\nLEFT JOIN orders o ON o.id=n.orderId\nWHERE n.userId=?\nORDER BY n.sentAt DESC\nLIMIT 20 OFFSET 0

DB --> NotificationService: Page<Notification>
deactivate NotificationService

Controller --> Customer: 200 OK\n{content: [{id, title, body,\ntype, orderStatus, sentAt, readAt}],\ntotalElements, totalPages}
deactivate Controller

== Get Unread Count ==
Customer -> Controller: GET /api/users/{userId}/notifications/unread
activate Controller

Controller -> NotificationService: getUnreadCount(userId)
activate NotificationService

NotificationService -> DB: SELECT COUNT(*)\nFROM notifications\nWHERE userId=? AND readAt IS NULL
DB --> NotificationService: count

NotificationService --> Controller: unreadCount
deactivate NotificationService

Controller --> Customer: 200 OK\n{unreadCount: 5}
deactivate Controller

== Mark as Read ==
Customer -> Controller: PUT /api/users/{userId}/notifications/{notificationId}/read
activate Controller

Controller -> NotificationService: markAsRead(notificationId)
activate NotificationService

NotificationService -> DB: UPDATE notifications SET\nreadAt=NOW()\nWHERE id=? AND userId=?
DB --> NotificationService: Updated

NotificationService --> Controller: Success
deactivate NotificationService

Controller --> Customer: 200 OK\n{message: "Marked as read"}
deactivate Controller

== Mark All as Read ==
Customer -> Controller: PUT /api/users/{userId}/notifications/read-all
activate Controller

Controller -> NotificationService: markAllAsRead(userId)
activate NotificationService

NotificationService -> DB: UPDATE notifications SET\nreadAt=NOW()\nWHERE userId=? AND readAt IS NULL
DB --> NotificationService: Updated count

NotificationService --> Controller: updatedCount
deactivate NotificationService

Controller --> Customer: 200 OK\n{message: "All notifications marked as read",\ncount: 5}
deactivate Controller

@enduml
```

## 5. FCM Token Management Flow

```plantuml
@startuml
title FCM Token Management Flow

actor Customer
participant "Mobile App" as App
participant "NotificationController" as Controller
participant "FcmService" as FcmService
participant "Database" as DB

== Save FCM Token on Login ==
App -> App: Firebase.initializeApp()
App -> "Firebase SDK": getToken()
activate "Firebase SDK"
"Firebase SDK" --> App: fcmToken
deactivate "Firebase SDK"

Customer -> App: Login successful
App -> Controller: PUT /api/users/{userId}/fcm-token\n{fcmToken, platform: "android",\ndeviceId: "device123"}
activate Controller

Controller -> FcmService: updateFcmToken(userId, token, platform, deviceId)
activate FcmService

FcmService -> DB: UPDATE users SET\nfcmToken=?,\nfcmPlatform=?,\nfcmDeviceId=?,\nfcmTokenUpdatedAt=NOW()\nWHERE id=?
DB --> FcmService: Token saved

FcmService --> Controller: Success
deactivate FcmService

Controller --> App: 200 OK\n{message: "FCM token saved"}
deactivate Controller

== Remove FCM Token on Logout ==
Customer -> App: Logout
App -> Controller: DELETE /api/users/{userId}/fcm-token
activate Controller

Controller -> FcmService: removeFcmToken(userId)
activate FcmService

FcmService -> DB: UPDATE users SET\nfcmToken=NULL,\nfcmPlatform=NULL,\nfcmDeviceId=NULL\nWHERE id=?
DB --> FcmService: Token removed

FcmService --> Controller: Success
deactivate FcmService

Controller --> App: 200 OK\n{message: "FCM token removed"}
deactivate Controller

== Handle Token Refresh ==
"Firebase SDK" -> App: onTokenRefresh(newToken)
activate App
App -> Controller: PUT /api/users/{userId}/fcm-token\n{fcmToken: newToken}
activate Controller

Controller -> FcmService: updateFcmToken(userId, newToken)
activate FcmService
FcmService -> DB: UPDATE users SET fcmToken=?
FcmService --> Controller: Updated
deactivate FcmService

Controller --> App: 200 OK
deactivate Controller
deactivate App

@enduml
```


