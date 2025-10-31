# Sequence Diagrams - Order Management Flows

## 1. Create Order Flow

```plantuml
@startuml
title Create Order Flow

actor Customer
participant "OrderController" as Controller
participant "OrderService" as OrderService
participant "BowlService" as BowlService
participant "InventoryService" as InventoryService
participant "FcmService" as FcmService
participant "NotificationService" as NotificationService
participant "Database" as DB

Customer -> Controller: POST /api/orders/create\n{userId, storeId, bowls[], note}
activate Controller

Controller -> OrderService: create(OrderRequest)
activate OrderService

OrderService -> DB: BEGIN TRANSACTION

' Validate store
OrderService -> DB: SELECT * FROM stores WHERE id=storeId
DB --> OrderService: Store found

' Create order
OrderService -> DB: INSERT INTO orders\n(userId, storeId, status=PENDING,\nsubtotalAmount=0, totalAmount=0)
DB --> OrderService: Order created

' Process each bowl
loop For each bowl in request
    OrderService -> BowlService: create(Bowl)
    activate BowlService
    
    BowlService -> DB: INSERT INTO bowls\n(orderId, templateId, name, linePrice)
    DB --> BowlService: Bowl created
    
    ' Add ingredients to bowl
    loop For each ingredient
        BowlService -> InventoryService: checkAvailability(ingredientId, quantity)
        activate InventoryService
        InventoryService -> DB: SELECT quantity FROM inventory\nWHERE ingredientId=? AND storeId=?
        DB --> InventoryService: Available quantity
        
        alt Insufficient stock
            InventoryService --> BowlService: Exception: Out of stock
            BowlService --> OrderService: Exception
            OrderService -> DB: ROLLBACK
            OrderService --> Controller: Error
            Controller --> Customer: 400 Bad Request\n"Ingredient out of stock"
        else Stock available
            InventoryService --> BowlService: OK
            deactivate InventoryService
            
            BowlService -> DB: INSERT INTO bowl_items\n(bowlId, ingredientId, quantity, linePrice)
            DB --> BowlService: Item added
        end
    end
    
    BowlService -> BowlService: Calculate bowl linePrice
    BowlService -> DB: UPDATE bowls SET linePrice=?
    
    BowlService --> OrderService: Bowl created with items
    deactivate BowlService
end

' Calculate order totals
OrderService -> OrderService: calculateTotals()
OrderService -> DB: UPDATE orders SET\nsubtotalAmount=?, totalAmount=?

OrderService -> DB: COMMIT TRANSACTION

' Send notification
OrderService -> FcmService: sendOrderNotification(order, PENDING)
activate FcmService
FcmService -> DB: SELECT fcm_token FROM users WHERE id=?
DB --> FcmService: fcmToken

FcmService -> FcmService: Build FCM message\n"🔔 Order Received\nYour order #{orderId} has been received"
FcmService -> "Firebase FCM": sendMessage(token, notification)
activate "Firebase FCM"
"Firebase FCM" --> FcmService: Message sent
deactivate "Firebase FCM"
deactivate FcmService

OrderService -> NotificationService: saveNotification(order, PENDING)
activate NotificationService
NotificationService -> DB: INSERT INTO notifications\n(userId, orderId, title, body, type, sentAt)
DB --> NotificationService: Notification saved
deactivate NotificationService

OrderService --> Controller: Order created
deactivate OrderService
Controller --> Customer: 201 Created\n{orderId, status, totalAmount}
deactivate Controller

@enduml
```

## 2. Update Order Status Flow

```plantuml
@startuml
title Update Order Status with Push Notification Flow

actor "Staff/Admin" as Staff
participant "OrderController" as Controller
participant "OrderService" as OrderService
participant "FcmService" as FcmService
participant "NotificationService" as NotificationService
participant "KitchenJobService" as KitchenService
participant "Database" as DB

Staff -> Controller: PUT /api/orders/{orderId}/status\n{status: "CONFIRMED"}
activate Controller

Controller -> OrderService: findById(orderId)
activate OrderService
OrderService -> DB: SELECT * FROM orders WHERE id=?
DB --> OrderService: Order found
deactivate OrderService

Controller -> OrderService: update(orderId, order)
activate OrderService

OrderService -> DB: UPDATE orders SET status=?\nWHERE id=?
DB --> OrderService: Order updated

alt Status = CONFIRMED
    OrderService -> KitchenService: createKitchenJob(order)
    activate KitchenService
    KitchenService -> DB: INSERT INTO kitchen_jobs\n(orderId, status=PENDING)
    DB --> KitchenService: Job created
    deactivate KitchenService
end

OrderService --> Controller: Updated order
deactivate OrderService

' Send push notification
Controller -> FcmService: sendOrderNotification(order, status)
activate FcmService

FcmService -> DB: SELECT fcm_token, user_id\nFROM users WHERE id=order.userId
DB --> FcmService: fcmToken, userId

FcmService -> FcmService: getNotificationMessage(status)
note right
  Status messages:
  - CONFIRMED: "✅ Order Confirmed - Estimated time: 30 mins"
  - PREPARING: "👨‍🍳 Chef is preparing your delicious meal!"
  - READY: "🎉 Order Ready for Pickup"
  - COMPLETED: "✨ Enjoy your meal! Rate your experience"
  - CANCELLED: "❌ Order cancelled. Refund processing"
end note

FcmService -> "Firebase FCM": sendMessage(fcmToken, notification)
activate "Firebase FCM"
"Firebase FCM" --> FcmService: Message sent successfully
deactivate "Firebase FCM"

FcmService --> Controller: Notification sent
deactivate FcmService

' Save notification to database
Controller -> NotificationService: saveNotification(userId, orderId, status)
activate NotificationService
NotificationService -> DB: INSERT INTO notifications\n(userId, orderId, title, body,\ntype=ORDER_UPDATE, orderStatus, sentAt)
DB --> NotificationService: Notification saved
deactivate NotificationService

Controller --> Staff: 200 OK\n{order with new status}
deactivate Controller

@enduml
```

## 3. Apply Promotion to Order Flow

```plantuml
@startuml
title Apply Promotion Code to Order Flow

actor Customer
participant "OrderController" as Controller
participant "OrderService" as OrderService
participant "PromotionService" as PromotionService
participant "PromotionRedemptionService" as RedemptionService
participant "Database" as DB

Customer -> Controller: POST /api/orders/apply-promo/{orderId}\n?code=FLASH50
activate Controller

Controller -> OrderService: applyPromotion(orderId, code)
activate OrderService

' Get order
OrderService -> DB: SELECT * FROM orders WHERE id=?
DB --> OrderService: Order found

' Validate promotion code
OrderService -> PromotionService: findByCode("FLASH50")
activate PromotionService
PromotionService -> DB: SELECT * FROM promotions\nWHERE UPPER(code)='FLASH50'\nAND isActive=true
DB --> PromotionService: Promotion found
deactivate PromotionService

OrderService -> PromotionService: validatePromotion(promotion, order)
activate PromotionService

PromotionService -> PromotionService: Check validFrom/validUntil dates
PromotionService -> PromotionService: Check usageLimit
PromotionService -> PromotionService: Check minOrderValue

alt Promotion invalid or expired
    PromotionService --> OrderService: Exception: Invalid promotion
    OrderService --> Controller: Error
    Controller --> Customer: 400 Bad Request\n"Promotion invalid or expired"
else Promotion valid
    PromotionService --> OrderService: Valid
    deactivate PromotionService
    
    ' Check if user already used this promotion
    OrderService -> RedemptionService: checkUserRedemption(userId, promotionId)
    activate RedemptionService
    RedemptionService -> DB: SELECT COUNT(*) FROM promotion_redemptions\nWHERE userId=? AND promotionId=?
    DB --> RedemptionService: Count
    
    alt Already redeemed
        RedemptionService --> OrderService: Exception: Already used
        OrderService --> Controller: Error
        Controller --> Customer: 400 Bad Request\n"Promotion already used"
    else Can redeem
        RedemptionService --> OrderService: Can redeem
        deactivate RedemptionService
        
        ' Calculate discount
        OrderService -> OrderService: calculateDiscount(order, promotion)
        note right
          If type = PERCENT_OFF:
            discount = subtotal * (percentOff / 100)
            discount = min(discount, maxDiscountAmount)
          If type = FIXED_AMOUNT_OFF:
            discount = fixedAmount
        end note
        
        ' Update order
        OrderService -> DB: BEGIN TRANSACTION
        OrderService -> DB: UPDATE orders SET\npromotionTotal=discount,\ntotalAmount=subtotal-discount\nWHERE id=?
        
        ' Create redemption record
        OrderService -> DB: INSERT INTO promotion_redemptions\n(orderId, promotionId, userId,\ndiscountAmount, status=APPLIED)
        
        ' Update promotion usage count
        OrderService -> DB: UPDATE promotions SET\ncurrentUsageCount=currentUsageCount+1\nWHERE id=?
        
        OrderService -> DB: COMMIT TRANSACTION
        
        OrderService --> Controller: Order with discount applied
        deactivate OrderService
        Controller --> Customer: 200 OK\n{order, discount, newTotal}
    end
end
deactivate Controller

@enduml
```

## 4. Complete Order Flow

```plantuml
@startuml
title Complete Order Flow

actor Customer
participant "OrderController" as Controller
participant "OrderService" as OrderService
participant "InventoryService" as InventoryService
participant "FcmService" as FcmService
participant "Database" as DB

Customer -> Controller: POST /api/orders/complete/{orderId}
activate Controller

Controller -> OrderService: complete(orderId)
activate OrderService

OrderService -> DB: SELECT * FROM orders o\nJOIN bowls b ON b.orderId=o.id\nJOIN bowl_items bi ON bi.bowlId=b.id\nWHERE o.id=?
DB --> OrderService: Order with all items

' Validate order can be completed
OrderService -> OrderService: checkStatus(order)
alt Status not READY
    OrderService --> Controller: Exception: Order not ready
    Controller --> Customer: 400 Bad Request\n"Order cannot be completed"
else Status is READY
    
    OrderService -> DB: BEGIN TRANSACTION
    
    ' Deduct inventory for each ingredient
    loop For each bowl_item
        OrderService -> InventoryService: deductStock(ingredientId, quantity, storeId)
        activate InventoryService
        
        InventoryService -> DB: UPDATE inventory SET\nquantity = quantity - ?\nWHERE ingredientId=? AND storeId=?
        
        InventoryService -> DB: INSERT INTO inventory\n(ingredientId, storeId, quantity,\naction=DEDUCT, reason='ORDER_COMPLETED')
        
        InventoryService --> OrderService: Stock deducted
        deactivate InventoryService
    end
    
    ' Update order status
    OrderService -> DB: UPDATE orders SET\nstatus='COMPLETED',\ncompletedAt=NOW()\nWHERE id=?
    
    OrderService -> DB: COMMIT TRANSACTION
    
    ' Send completion notification
    OrderService -> FcmService: sendOrderNotification(order, COMPLETED)
    activate FcmService
    FcmService -> "Firebase FCM": sendMessage\n"✨ Enjoy your meal! Rate your experience"
    deactivate FcmService
    
    OrderService --> Controller: Order completed
    deactivate OrderService
    Controller --> Customer: 200 OK\n{order, status=COMPLETED}
end
deactivate Controller

@enduml
```

## 5. Cancel Order Flow

```plantuml
@startuml
title Cancel Order Flow

actor Customer
participant "OrderController" as Controller
participant "OrderService" as OrderService
participant "PaymentService" as PaymentService
participant "FcmService" as FcmService
participant "Database" as DB

Customer -> Controller: POST /api/orders/cancel/{orderId}\n?reason=Changed mind
activate Controller

Controller -> OrderService: cancel(orderId, reason)
activate OrderService

OrderService -> DB: SELECT * FROM orders WHERE id=?
DB --> OrderService: Order found

' Validate order can be cancelled
OrderService -> OrderService: checkStatus(order)
alt Status is COMPLETED or CANCELLED
    OrderService --> Controller: Exception: Cannot cancel
    Controller --> Customer: 400 Bad Request\n"Order cannot be cancelled"
else Status allows cancellation
    
    OrderService -> DB: BEGIN TRANSACTION
    
    ' Update order status
    OrderService -> DB: UPDATE orders SET\nstatus='CANCELLED',\ncancellationReason=?\nWHERE id=?
    
    ' Check if payment was made
    OrderService -> DB: SELECT * FROM payment_transactions\nWHERE orderId=? AND status='COMPLETED'
    DB --> OrderService: Payment found
    
    alt Payment exists
        ' Initiate refund
        OrderService -> PaymentService: initiateRefund(orderId)
        activate PaymentService
        PaymentService -> DB: INSERT INTO payment_transactions\n(orderId, amount, method, status='REFUND_PENDING')
        PaymentService --> OrderService: Refund initiated
        deactivate PaymentService
    end
    
    OrderService -> DB: COMMIT TRANSACTION
    
    ' Send cancellation notification
    OrderService -> FcmService: sendOrderNotification(order, CANCELLED)
    activate FcmService
    FcmService -> "Firebase FCM": sendMessage\n"❌ Order cancelled. Refund will be processed within 3-5 days"
    deactivate FcmService
    
    OrderService --> Controller: Order cancelled
    deactivate OrderService
    Controller --> Customer: 200 OK\n{order, status=CANCELLED, refundStatus}
end
deactivate Controller

@enduml
```

## 6. View Order History Flow

```plantuml
@startuml
title View Order History Flow

actor Customer
participant "OrderController" as Controller
participant "OrderService" as OrderService
participant "OrderMapper" as Mapper
participant "Database" as DB

Customer -> Controller: GET /api/orders/order-history/{userId}
activate Controller

Controller -> OrderService: findByUserId(userId)
activate OrderService

OrderService -> DB: SELECT o.*, s.name as storeName,\nb.*, bi.*, i.name as ingredientName\nFROM orders o\nLEFT JOIN stores s ON s.id=o.storeId\nLEFT JOIN bowls b ON b.orderId=o.id\nLEFT JOIN bowl_items bi ON bi.bowlId=b.id\nLEFT JOIN ingredients i ON i.id=bi.ingredientId\nWHERE o.userId=?\nORDER BY o.createdAt DESC

DB --> OrderService: List<Order> with relations

OrderService --> Controller: List<Order>
deactivate OrderService

' Map to response DTOs
Controller -> Mapper: toResponse(orders)
activate Mapper
loop For each order
    Mapper -> Mapper: Map order fields
    Mapper -> Mapper: Map nested bowls
    Mapper -> Mapper: Map bowl items with ingredients
end
Mapper --> Controller: List<OrderResponse>
deactivate Mapper

Controller --> Customer: 200 OK\n[{orderId, status, totalAmount,\nbowls, createdAt, ...}]
deactivate Controller

@enduml
```


