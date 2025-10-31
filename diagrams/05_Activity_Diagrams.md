# Activity Diagrams - Business Process Flows

## 1. User Registration and Verification Process

```plantuml
@startuml
title User Registration and Email Verification Activity Diagram

start

:User enters registration details\n(email, password, fullName);

:Submit registration form;

:System validates input;

if (Email already exists?) then (yes)
  :Return error\n"Email already registered";
  stop
else (no)
  :Generate 6-digit OTP;
  
  :Create user account with\nstatus = PENDING_VERIFICATION;
  
  :Save OTP and expiry time\n(valid for 5 minutes);
  
  fork
    :Send OTP email\nusing HTML template;
  fork again
    :Save user to database;
  end fork
  
  :Return success response\n"OTP sent to email";
endif

:User receives email;

:User enters OTP code;

:Submit OTP verification;

if (OTP matches?) then (no)
  :Increment failed attempts;
  if (Attempts >= 5?) then (yes)
    :Lock account temporarily;
    :Return error\n"Too many attempts";
    stop
  else (no)
    :Return error\n"Invalid OTP";
    stop
  endif
else (yes)
  if (OTP expired?) then (yes)
    :Return error\n"OTP expired\nPlease request new OTP";
    stop
  else (no)
    :Update user status = ACTIVE;
    
    :Set emailVerified = true;
    
    :Clear OTP fields;
    
    :Return success\n"Email verified successfully";
    
    stop
  endif
endif

@enduml
```

## 2. Create Order Process

```plantuml
@startuml
title Create Order Activity Diagram

start

:User browses bowl templates;

:User selects a template;

partition "Build Custom Bowl" {
  repeat
    :Add ingredient to bowl;
    
    :Calculate ingredient price;
    
    :Update bowl total;
  repeat while (More ingredients?) is (yes)
  -> no;
}

:Review bowl composition;

if (Add another bowl?) then (yes)
  -[#blue]-> repeat bowl creation;
else (no)
  :Enter order details\n(pickup time, note);
  
  :Submit order;
  
  :System validates order;
  
  if (User authenticated?) then (no)
    :Return error\n"Authentication required";
    stop
  endif
  
  if (Store available?) then (no)
    :Return error\n"Store not found";
    stop
  endif
  
  partition "Process Order" #LightBlue {
    :Start database transaction;
    
    :Create order record\nwith status = PENDING;
    
    fork
      repeat :For each bowl;
        :Create bowl record;
        
        repeat :For each ingredient;
          :Check inventory availability;
          
          if (Stock sufficient?) then (no)
            :Rollback transaction;
            :Return error\n"Ingredient out of stock";
            kill
          else (yes)
            :Create bowl_item record;
            
            :Calculate line price;
          endif
        repeat while (More ingredients?)
        
        :Calculate bowl total price;
      repeat while (More bowls?)
    fork again
      :Calculate order subtotal;
      
      if (Promotion code applied?) then (yes)
        :Validate promotion;
        
        if (Valid promotion?) then (yes)
          :Calculate discount;
          
          :Create redemption record;
          
          :Update order promotionTotal;
        else (no)
          :Skip promotion;
        endif
      endif
    end fork
    
    :Calculate final totalAmount;
    
    :Update order totals;
    
    :Commit transaction;
  }
  
  fork
    :Save order to database;
  fork again
    :Send push notification\n"🔔 Order Received";
  fork again
    :Save notification to history;
  end fork
  
  :Return order confirmation\n{orderId, status, totalAmount};
  
  stop
endif

@enduml
```

## 3. Order Lifecycle Process

```plantuml
@startuml
title Order Lifecycle Activity Diagram

|Customer|
start
:Create order;

|System|
:Set status = PENDING;

fork
  :Send notification\n"Order Received";
fork again
  :Wait for admin/staff action;
end fork

|Admin/Staff|
:Review new order;

if (Accept order?) then (yes)
  |System|
  :Update status = CONFIRMED;
  
  fork
    :Send notification\n"✅ Order Confirmed";
  fork again
    :Create kitchen job;
  end fork
  
  |Kitchen Staff|
  :View kitchen job;
  
  :Start preparing order;
  
  |System|
  :Update status = PREPARING;
  
  :Send notification\n"👨‍🍳 Chef is preparing";
  
  |Kitchen Staff|
  :Complete preparation;
  
  :Mark job as completed;
  
  |System|
  :Update status = READY;
  
  :Send notification\n"🎉 Order Ready for Pickup";
  
  |Customer|
  :Go to restaurant;
  
  :Pick up order;
  
  |Staff|
  :Confirm pickup;
  
  |System|
  :Update status = COMPLETED;
  
  fork
    :Deduct inventory stock;
  fork again
    :Send notification\n"✨ Enjoy your meal!";
  end fork
  
  stop
  
else (no)
  |System|
  :Update status = CANCELLED;
  
  if (Payment made?) then (yes)
    :Initiate refund process;
    
    fork
      :Create refund transaction;
    fork again
      :Send notification\n"❌ Order cancelled\nRefund processing";
    end fork
  else (no)
    :Send cancellation notification;
  endif
  
  stop
endif

@enduml
```

## 4. Payment Processing Activity

```plantuml
@startuml
title ZaloPay Payment Processing Activity Diagram

|Customer|
start
:Review order details;

:Click "Pay with ZaloPay";

|System|
:Validate order exists;

:Create payment transaction\nstatus = PENDING;

:Generate app_trans_id\n(YYMMDD_orderId);

:Build ZaloPay request payload;

:Calculate HMAC-SHA256 signature;

:Call ZaloPay API\nPOST /createorder;

|ZaloPay Gateway|
:Validate request signature;

:Validate merchant credentials;

if (Valid request?) then (no)
  :Return error response;
  
  |System|
  :Update transaction\nstatus = FAILED;
  
  |Customer|
  :Show error message;
  stop
else (yes)
  :Generate payment URL;
  
  :Return order_url and zp_trans_token;
  
  |System|
  :Return payment URL to app;
  
  |Customer|
  :Open ZaloPay payment page;
  
  :Authenticate with ZaloPay;
  
  :Review payment details;
  
  if (Confirm payment?) then (no)
    :Cancel payment;
    
    |Customer|
    :Return to app;
    stop
  else (yes)
    :Submit payment;
    
    |ZaloPay Gateway|
    :Process payment;
    
    if (Payment successful?) then (yes)
      fork
        :Send callback to server\nPOST /zalopay/callback;
      fork again
        :Redirect user to app\nwith success status;
      end fork
      
      |System|
      :Receive callback;
      
      :Verify callback MAC signature;
      
      if (Valid signature?) then (yes)
        :Update payment transaction\nstatus = COMPLETED;
        
        :Update order\npaymentStatus = PAID;
        
        :Return success to ZaloPay;
        
        |Customer|
        :Receive success message;
        
        :View order status;
        
        stop
      else (no)
        :Reject callback;
        
        :Log security warning;
        
        stop
      endif
    else (no)
      :Send failure callback;
      
      |System|
      :Update transaction\nstatus = FAILED;
      
      |Customer|
      :Show payment failed message;
      
      stop
    endif
  endif
endif

@enduml
```

## 5. Apply Promotion Code Activity

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


