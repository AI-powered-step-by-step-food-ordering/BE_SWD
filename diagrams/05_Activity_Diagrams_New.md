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

---

## 5. Apply Promotion to Order

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

title Apply Promotion Code to Order

|Customer|
start
:View order details;
:Enter promotion code;
:Submit code;
note right: POST /api/orders/apply-promo/{orderId}?code=XXX

|System|
:Validate order exists;

if (Order found?) then (no)
  :Return "Order not found"<< Error >>;
  stop
endif

if (Order status = PENDING?) then (no)
  :Return "Can only apply to PENDING orders"<< Error >>;
  stop
endif

:Search promotion by code;

if (Promotion exists?) then (no)
  :Return "Invalid promotion code"<< Error >>;
  stop
endif

:Validate promotion;

if (Promotion active?) then (no)
  :Return "Promotion inactive"<< Error >>;
  stop
endif

if (Current date within starts_at & ends_at?) then (no)
  :Return "Promotion expired"<< Error >>;
  stop
endif

:Check if promotion already applied;

if (Already applied?) then (yes)
  :Return "Promotion already applied"<< Error >>;
  stop
endif

:Calculate discount amount
= subtotal × discount_percent / 100;
:Update order.promotionTotal;
:Update order.totalAmount
= subtotal - discount;
:Create promotion_redemption
status = APPLIED<< Processing >>;
:Save to database;

fork
  :Send notification
  "🎉 Promotion applied!"<< Success >>;
fork again
  :Log promotion usage;
end fork

|Customer|
:See updated order total
with discount applied<< Success >>;
stop

@enduml
```

---

## 6. Ingredient Inventory Management

```plantuml
@startuml
skinparam backgroundColor #FEFEFE
skinparam activity {
  BackgroundColor<< Success >> #C8E6C9
  BackgroundColor<< Warning >> #FFF9C4
  BackgroundColor<< Error >> #FFCDD2
  BorderColor DarkSlateGray
  FontName Arial
}

title Ingredient Inventory Update Process

|Admin/Staff|
start
:Access inventory management;
:Select ingredient to update;
note right: PUT /api/ingredients/update/{id}

|System|
:Load ingredient details;
:Display current stock level;

|Admin/Staff|
:Review current quantity;

if (Stock update type?) then (Add Stock)
  :Enter quantity to add;
  note right: Receiving new supplies
  
  |System|
  :Add to current stock;
  :Update ingredient.quantity;
  :Log inventory transaction
  type = STOCK_IN;
  
else (Adjust Stock)
  :Enter new quantity;
  note right: Manual adjustment
  
  |System|
  :Replace current quantity;
  :Log inventory transaction
  type = ADJUSTMENT;
  
else (Deduct Stock)
  :Enter quantity to deduct;
  note right: Wastage or damage
  
  |System|
  :Subtract from current stock;
  
  if (Result < 0?) then (yes)
    :Return "Insufficient stock"<< Error >>;
    stop
  endif
  
  :Update ingredient.quantity;
  :Log inventory transaction
  type = STOCK_OUT;
endif

:Calculate new stock level;

if (Stock < minimum threshold?) then (yes)
  :Set low stock alert<< Warning >>;
  fork
    :Send notification to admin
    "⚠️ Low stock alert";
  fork again
    :Add to replenishment list;
  end fork
endif

:Save changes to database;

|Admin/Staff|
:View updated inventory<< Success >>;
:Generate stock report;
stop

@enduml
```

---

## 7. Bowl Template Creation with Default Ingredients

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

title Create Bowl Template with Pre-configured Ingredients

|Admin|
start
:Access template management;
:Click "Create New Template";
:Enter template info
(name, description, image);
note right: POST /api/bowl_templates/create

|System|
:Validate template data;

if (Template name unique?) then (no)
  :Return "Template name exists"<< Error >>;
  stop
endif

:Create bowl_template record;

|Admin|
partition "Configure Steps" {
  :Add Step 1 (Category);
  :Set minItems, maxItems, defaultQty;
  note right: POST /api/template_steps/create
  
  |System|
  :Validate category exists;
  :Create template_step;
  
  |Admin|
  :Select default ingredients;
  :Set quantity for each;
  note right: POST /api/default_ingredients/create-bulk
  
  |System|
  :Validate ingredients in category;
  
  if (Ingredients valid?) then (no)
    :Return "Invalid ingredient"<< Error >>;
    stop
  endif
  
  :Create default_ingredient records;
  
  |Admin|
  :Repeat for other steps
  (Protein, Veggie, Sauce...);
}

|System|
:Calculate default price
= SUM(ingredient.unitPrice × quantity);
:Update template.defaultPrice;
:Save all changes;

fork
  :Generate preview;
fork again
  :Index for search;
end fork

|Admin|
:Review template preview<< Success >>;
:Publish template
Set active = true;

|System|
:Make available to customers;

|Customer|
:See new template in menu;
stop

@enduml
```

---

## 8. AI Bowl Analysis for Nutritional Goals

```plantuml
@startuml
skinparam backgroundColor #FEFEFE
skinparam activity {
  BackgroundColor<< Success >> #C8E6C9
  BackgroundColor<< Error >> #FFCDD2
  BackgroundColor<< AI >> #E1BEE7
  BorderColor DarkSlateGray
  FontName Arial
}

title AI-Powered Bowl Analysis Process

|Customer|
start
:Create custom bowl;
:Add/remove ingredients;
:Request AI analysis;
note right: POST /api/ai/analyze-bowl

|System|
:Collect bowl data
(all ingredients + quantities);

partition "Calculate Nutrition" {
  :Sum calories;
  :Sum protein (g);
  :Sum carbs (g);
  :Sum fats (g);
  :Sum fiber (g);
  :Calculate macro percentages;
}

:Prepare analysis request
{
  calories, protein, carbs, fats,
  fiber, ingredients_list,
  health_goal: user's goal
};

|OpenAI Service|
:Send to ChatGPT API<< AI >>;
note right: gpt-4o-mini model

:Analyze nutritional balance;
:Compare with health goals;
:Generate recommendations;

if (API call successful?) then (no)
  |System|
  :Log error;
  :Return "Analysis unavailable"<< Error >>;
  stop
endif

:Return AI analysis
{
  overallScore (0-100),
  strengths[],
  improvements[],
  recommendations[],
  emoji
};

|System|
:Parse AI response;
:Format for display;
:Save analysis history;

|Customer|
:View analysis results<< Success >>;

if (Satisfied with bowl?) then (yes)
  :Add to order;
  stop
else (no)
  :Modify ingredients
  based on recommendations;
  :Request new analysis;
  note right: Iterative improvement
endif

@enduml
```

---

## 9. User Password Reset Flow

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

title Password Reset with OTP Verification

|Customer|
start
:Click "Forgot Password";
:Enter registered email;
:Submit request;
note right: POST /api/auth/forgot-password

|System|
:Search user by email;

if (User exists?) then (no)
  :Return "Email not found"<< Error >>;
  note right: Security: Don't reveal if email exists
  stop
endif

if (User status = ACTIVE?) then (no)
  :Return "Account not active"<< Error >>;
  stop
endif

:Generate 6-digit OTP;
:Set expiry = now + 5 minutes;
:Save OTP to cache/database;

|Email Service|
:Send password reset email
with OTP code<< Processing >>;

if (Email sent successfully?) then (no)
  |System|
  :Return "Failed to send email"<< Error >>;
  stop
endif

|System|
:Return "OTP sent to email"<< Success >>;

|Customer|
:Receive email;
:Open email;
:Copy OTP code;
:Enter OTP + new password;
:Submit;
note right: POST /api/auth/reset-password

|System|
:Validate OTP;

if (OTP matches & not expired?) then (no)
  :Increment failed attempts;
  
  if (Failed attempts >= 5?) then (yes)
    :Block further attempts
    for 30 minutes<< Error >>;
    :Return "Too many attempts";
    stop
  endif
  
  :Return "Invalid or expired OTP"<< Error >>;
  stop
endif

:Validate new password
(min 8 chars, complexity);

if (Password valid?) then (no)
  :Return "Password too weak"<< Error >>;
  stop
endif

:Hash new password
(BCrypt);
:Update user.password;
:Invalidate OTP;
:Clear failed attempts;
:Save to database;

fork
  :Send confirmation email
  "Password changed successfully";
fork again
  :Revoke all existing tokens
  (force re-login);
fork again
  :Log security event;
end fork

|Customer|
:See success message<< Success >>;
:Redirected to login page;
:Login with new password;
stop

@enduml
```

---

## Additional Notes

**New Diagrams Cover:**
1. **Promotion Application** - Discount code validation and redemption
2. **Inventory Management** - Stock updates with low-stock alerts
3. **Template Creation** - Bowl template with default ingredients setup
4. **AI Bowl Analysis** - Integration with OpenAI for nutritional recommendations
5. **Password Reset** - Secure OTP-based password recovery

**Key Patterns:**
- 🏊 **Swimlanes** clearly show responsibility separation
- ✅ **Validation gates** at each critical step
- 🔔 **Async notifications** with fork/join
- 🔒 **Security checks** (OTP limits, authentication)
- 📊 **Business logic** (calculations, stock checks)
- 🎨 **Color coding** for different states

**Related API Endpoints:**
- `POST /api/orders/apply-promo/{orderId}` - Apply promotion
- `PUT /api/ingredients/update/{id}` - Update inventory
- `POST /api/bowl_templates/create` - Create template
- `POST /api/default_ingredients/create-bulk` - Add defaults
- `POST /api/ai/analyze-bowl` - AI analysis
- `POST /api/auth/forgot-password` - Request reset
- `POST /api/auth/reset-password` - Complete reset

