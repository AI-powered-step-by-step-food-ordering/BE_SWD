# Activity Diagrams - Business Process Flows (Simplified & Enhanced)

## 1. User Registration and Email Verification

```plantuml
@startuml
skinparam backgroundColor #FEFEFE
skinparam activity {
  BackgroundColor<< Success >> LightGreen
  BackgroundColor<< Error >> LightCoral
  BorderColor DarkSlateGray
  FontName Arial
}

title User Registration and Email Verification Process

start
:User submits registration
(email, password, fullName);
note right: POST /api/auth/register

if (Email already exists?) then (yes)
  :Return error
  "Email already registered"<< Error >>;
  stop
endif

:Generate 6-digit OTP;
:Create account
status = PENDING_VERIFICATION;
:Send OTP email
(valid 5 minutes);
:Return success<< Success >>;

:User receives email;
:User submits OTP;
note right: POST /api/auth/verify-otp

if (OTP valid & not expired?) then (no)
  if (Failed attempts >= 5?) then (yes)
    :Return "Too many attempts"<< Error >>;
    stop
  endif
  :Return "Invalid OTP"<< Error >>;
  stop
endif

:Update status = ACTIVE
Set emailVerified = true;
:Return "Email verified"<< Success >>;
stop

@enduml
```

## 2. Create Order Process

```plantuml
@startuml
skinparam backgroundColor #FEFEFE
skinparam activity {
  BackgroundColor<< Success >> LightGreen
  BackgroundColor<< Error >> LightCoral
  BackgroundColor<< Processing >> LightBlue
  BorderColor DarkSlateGray
  FontName Arial
}

title Create Custom Bowl Order Process

|Customer|
start
:Browse bowl templates;
:Select template & ingredients;
note right: Build custom bowl

|System|
:Validate user authentication;

if (User authenticated?) then (no)
  :Return "Login required"<< Error >>;
  stop
endif

:Create order
status = PENDING<< Processing >>;

partition "Process Each Bowl" {
  :Create bowl records;
  :Add ingredients (bowl_items);
  :Check inventory availability;
  
  if (Stock sufficient?) then (no)
    :Return "Out of stock"<< Error >>;
    stop
  endif
  
  :Calculate bowl price;
}

:Calculate subtotal & total;
:Save order to database;

fork
  :Send notification
  "🔔 Order Received"<< Success >>;
fork again
  :Save notification history;
end fork

|Customer|
:Receive order confirmation;
stop

@enduml
```

## 3. Order Status Management

```plantuml
@startuml
skinparam backgroundColor #FEFEFE
skinparam activityBackgroundColor<< Pending >> #FFF4E6
skinparam activityBackgroundColor<< Confirmed >> #E3F2FD
skinparam activityBackgroundColor<< Preparing >> #FFF9C4
skinparam activityBackgroundColor<< Ready >> #C8E6C9
skinparam activityBackgroundColor<< Completed >> #A5D6A7

title Order Lifecycle Management

|Customer|
start
:Create order;

|System|
:Set status = PENDING<< Pending >>;
:Send "Order Received" notification;

|Admin/Staff|
:Review order;

if (Accept order?) then (no)
  |System|
  :Set status = CANCELLED;
  :Send cancellation notification;
  stop
endif

|System|
:Set status = CONFIRMED<< Confirmed >>;
:Create kitchen job;
:Send "Order Confirmed ✅" notification;

|Kitchen Staff|
:Start food preparation;

|System|
:Set status = PREPARING<< Preparing >>;
:Send "Chef is preparing 👨‍🍳" notification;

|Kitchen Staff|
:Complete preparation;
:Mark job as completed;

|System|
:Set status = READY<< Ready >>;
:Send "Ready for pickup 🎉" notification;

|Customer|
:Arrive at store;
:Pick up order;

|Staff|
:Confirm customer pickup;

|System|
:Set status = COMPLETED<< Completed >>;
:Deduct inventory stock;
:Send "Enjoy your meal! ✨" notification;

stop

@enduml
```

## 4. ZaloPay Payment Processing

```plantuml
@startuml
skinparam backgroundColor #FEFEFE
skinparam activity {
  BackgroundColor<< Success >> #C8E6C9
  BackgroundColor<< Error >> #FFCDD2
  BackgroundColor<< Processing >> #BBDEFB
  BorderColor DarkSlateGray
  FontName Arial
}

title ZaloPay Payment Integration

|Customer|
start
:Review order;
:Click "Pay with ZaloPay";

|System|
:Create payment transaction
status = PENDING<< Processing >>;
:Generate app_trans_id;
:Calculate HMAC-SHA256 signature;
:Call ZaloPay API
POST /createorder;

|ZaloPay|
:Validate request signature;

if (Valid request?) then (no)
  :Return error;
  |System|
  :Update status = FAILED<< Error >>;
  stop
endif

:Generate payment URL;
:Return payment URL & token;

|Customer|
:Open ZaloPay app/web;
:Authenticate in ZaloPay;
:Review payment details;

if (Confirm payment?) then (no)
  :Cancel payment;
  stop
endif

:Submit payment;

|ZaloPay|
:Process payment;

if (Payment successful?) then (yes)
  fork
    :Send callback to server
    POST /zalopay/callback;
  fork again
    :Redirect customer
    to app with success;
  end fork
  
  |System|
  :Verify callback MAC signature;
  
  if (Valid signature?) then (yes)
    :Update transaction
    status = COMPLETED<< Success >>;
    :Update order
    paymentStatus = PAID;
    :Return success to ZaloPay;
    
    |Customer|
    :See payment success message;
    stop
  else (no)
    :Reject callback;
    :Log security warning<< Error >>;
    stop
  endif
else (no)
  :Send failure callback;
  
  |System|
  :Update status = FAILED<< Error >>;
  
  |Customer|
  :See payment failed message;
  stop
endif

@enduml
```

---

## Notes

**Key Features:**
- ✅ Simplified flow with clear decision points
- 🎨 Color-coded status indicators
- 📱 Push notification integration
- 🔒 Payment security with MAC signature verification
- 📊 Inventory management integration
- 🍜 Kitchen job workflow

**API Endpoints:**
- `POST /api/auth/register` - User registration
- `POST /api/auth/verify-otp` - Email verification
- `POST /api/orders/create` - Create order
- `POST /api/orders/confirm/{id}` - Confirm order
- `POST /api/orders/complete/{id}` - Complete order
- `POST /api/zalopay/create-payment` - Initiate payment
- `POST /api/zalopay/callback` - Payment callback

---

## Additional Diagrams (Optional Reference)

### 5. Apply Promotion Code Activity

```plantuml
@startuml
title Apply Promotion Code Activity Diagram

|Customer|
start
:Enter promotion code;

:Click "Apply";

|System|
:Receive request with\n(orderId, promoCode);

:Fetch order details;

if (Order exists?) then (no)
  :Return error\n"Order not found";
  stop
endif

:Normalize code to UPPERCASE;

:Search promotion by code;

if (Promotion found?) then (no)
  :Return error\n"Invalid promotion code";
  stop
endif

:Validate promotion rules;

partition "Validation Checks" {
  if (Promotion active?) then (no)
    :Return error\n"Promotion is inactive";
    stop
  endif
  
  if (Within valid date range?) then (no)
    :Return error\n"Promotion expired";
    stop
  endif
  
  if (Order meets min value?) then (no)
    :Return error\n"Order amount too low";
    stop
  endif
  
  if (Usage limit reached?) then (yes)
    :Return error\n"Promotion fully redeemed";
    stop
  endif
  
  :Check user redemption history;
  
  if (User already used?) then (yes)
    :Return error\n"Already used this promotion";
    stop
  endif
}

:Calculate discount amount;

if (Promotion type?) then (PERCENT_OFF)
  :discount = subtotal × (percentOff / 100);
  
  if (Exceeds maxDiscount?) then (yes)
    :Cap at maxDiscountAmount;
  endif
else (FIXED_AMOUNT_OFF)
  :discount = fixedAmount;
endif

:Start database transaction;

fork
  :Update order\npromotionTotal = discount\ntotalAmount = subtotal - discount;
fork again
  :Create redemption record\n(orderId, promotionId, userId,\ndiscountAmount, status=APPLIED);
fork again
  :Increment promotion\ncurrentUsageCount;
end fork

:Commit transaction;

:Return success response\n{order, discountApplied, newTotal};

|Customer|
:View updated order total;

stop

@enduml
```

## 6. Kitchen Job Management Activity

```plantuml
@startuml
title Kitchen Job Management Activity Diagram

|System|
start
:Order status changed to CONFIRMED;

:Create kitchen job\nstatus = PENDING;

:Assign to kitchen queue;

|Kitchen Staff|
:View pending jobs dashboard;

:Select a job to work on;

:Update job status = IN_PROGRESS;

:Prepare ingredients;

repeat
  :Check ingredient from job items;
  
  :Verify ingredient available;
  
  if (Ingredient available?) then (no)
    :Mark ingredient as missing;
    
    :Notify inventory manager;
    
    if (Can substitute?) then (yes)
      :Use alternative ingredient;
    else (no)
      :Update job status = BLOCKED;
      
      :Notify customer about issue;
      
      stop
    endif
  endif
  
  :Prepare ingredient portion;
  
  :Add to bowl;
  
repeat while (More ingredients?) is (yes)
-> no;

:Assemble bowl;

:Quality check;

if (Quality acceptable?) then (no)
  :Remake bowl;
  -[#red]-> repeat preparation;
else (yes)
  :Package order;
  
  :Update job status = COMPLETED;
  
  |System|
  :Update order status = READY;
  
  fork
    :Send push notification\n"Order ready for pickup";
  fork again
    :Update job completedAt timestamp;
  fork again
    :Calculate preparation time;
  end fork
  
  |Customer|
  :Receive notification;
  
  :Head to restaurant for pickup;
  
  stop
endif

@enduml
```

## 7. Inventory Management Activity

```plantuml
@startuml
title Inventory Management Activity Diagram

|Kitchen Staff|
start

if (Action type?) then (Add Stock)
  :Receive ingredient delivery;
  
  :Scan/Enter ingredient barcode;
  
  :Enter quantity to add;
  
  :Enter expiry date (if applicable);
  
  |System|
  :Validate ingredient exists;
  
  :Start transaction;
  
  :Update inventory\nquantity = quantity + added;
  
  :Create inventory log\n(action=ADD, quantity, reason);
  
  :Commit transaction;
  
  :Return success;
  
  |Kitchen Staff|
  :View updated stock level;
  
  stop
  
else (Deduct Stock)
  :Order completed;
  
  |System|
  :Fetch order bowl items;
  
  repeat :For each ingredient used;
    :Calculate total quantity;
    
    :Start transaction;
    
    :Check current stock level;
    
    if (Stock sufficient?) then (no)
      :Rollback transaction;
      
      :Log inventory error;
      
      :Send alert to manager;
      
      stop
    else (yes)
      :Update inventory\nquantity = quantity - used;
      
      :Create inventory log\n(action=DEDUCT, quantity,\nreason=ORDER_COMPLETED);
      
      if (Stock below threshold?) then (yes)
        :Trigger low stock alert;
        
        :Create purchase order suggestion;
      endif
    endif
  repeat while (More ingredients?)
  
  :Commit transaction;
  
  stop
  
else (Check Stock)
  |Kitchen Staff|
  :Navigate to inventory page;
  
  |System|
  :Fetch all inventory items;
  
  :Calculate stock levels;
  
  partition "Display Information" {
    :Show ingredient name;
    :Show current quantity;
    :Show unit of measure;
    :Show last updated time;
    
    if (Stock level low?) then (yes)
      :Highlight in red;
    else if (Stock level medium?) then (yes)
      :Highlight in yellow;
    else (sufficient)
      :Highlight in green;
    endif
  }
  
  |Kitchen Staff|
  :Review stock status;
  
  if (Need to reorder?) then (yes)
    :Create purchase order;
    
    :Submit to supplier;
  endif
  
  stop
  
else (Adjust Stock)
  |Kitchen Staff|
  :Physical inventory count;
  
  :Enter actual quantity;
  
  :Enter adjustment reason;
  
  |System|
  :Calculate difference;
  
  :difference = actual - system;
  
  if (Large discrepancy?) then (yes)
    :Require manager approval;
    
    |Manager|
    :Review adjustment;
    
    if (Approve?) then (no)
      :Reject adjustment;
      
      :Request recount;
      
      stop
    endif
  endif
  
  :Update inventory quantity;
  
  :Create inventory log\n(action=ADJUST, difference, reason);
  
  stop
endif

@enduml
```


