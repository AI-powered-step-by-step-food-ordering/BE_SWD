# Activity Diagrams - Business Process Flows (Simplified)

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

---

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

---

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

---

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

