# State Diagrams - Main Objects

## 1. User Account State Diagram

```plantuml
@startuml
title User Account State Machine

[*] --> PendingVerification : Register

state PendingVerification {
  [*] --> AwaitingOTP
  AwaitingOTP : Entry: Generate OTP
  AwaitingOTP : Entry: Send verification email
  AwaitingOTP : otpAttempts = 0
  
  AwaitingOTP --> OTPSent : Email sent successfully
  
  OTPSent --> AwaitingOTP : Resend OTP requested
  OTPSent --> [*] : OTP verified
}

PendingVerification --> Active : Verify OTP\n[OTP valid && not expired]

Active : Entry: Set emailVerified = true
Active : Entry: Set status = ACTIVE
Active : Can place orders
Active : Can update profile

Active --> Active : Update profile
Active --> Active : Change password
Active --> Active : Login/Logout

Active --> Suspended : Admin suspends\n[rule violation]

Suspended : Entry: Set status = SUSPENDED
Suspended : Cannot login
Suspended : Cannot place orders

Suspended --> Active : Admin reactivates\n[issue resolved]

Active --> Deleted : User deletes account\n[soft delete]
Active --> Deleted : Admin deletes\n[policy violation]

Deleted : Entry: Set status = DELETED
Deleted : Entry: Set deletedAt = NOW()
Deleted : Entry: Clear FCM token
Deleted : Cannot login
Deleted : Data retained for audit

state PasswordReset {
  [*] --> ResetRequested : Forgot password
  
  ResetRequested : Entry: Generate reset OTP
  ResetRequested : Entry: Send reset email
  
  ResetRequested --> OTPValidated : Enter valid OTP\n[not expired]
  
  OTPValidated --> [*] : Set new password
  
  ResetRequested --> [*] : OTP expired\n[after 5 minutes]
}

Active --> PasswordReset : Request password reset
PasswordReset --> Active : Password reset successful

PendingVerification --> Deleted : Never verified\n[after 30 days]

note right of PendingVerification
  - OTP valid for 5 minutes
  - Max 5 attempts
  - Can resend OTP
end note

note right of Active
  - Can perform all user actions
  - FCM token saved on login
  - FCM token removed on logout
end note

note right of Deleted
  - Soft delete (deletedAt != null)
  - Cannot be reactivated
  - Email can be reused after 90 days
end note

@enduml
```

## 2. Order State Diagram

```plantuml
@startuml
title Order State Machine

[*] --> Pending : Create order

Pending : Entry: Create order record
Pending : Entry: Create bowl items
Pending : Entry: Calculate totals
Pending : Entry: Send "Order Received" notification
Pending : Waiting for admin/staff confirmation

Pending --> Confirmed : Admin confirms\n[order valid]
Pending --> Cancelled : Customer cancels\n[before confirmation]
Pending --> Cancelled : Admin rejects\n[invalid order / store closed]

Confirmed : Entry: Send "Order Confirmed" notification
Confirmed : Entry: Create kitchen job
Confirmed : Estimated time: 30 mins
Confirmed : Payment may be pending

state Confirmed {
  [*] --> AwaitingPayment
  AwaitingPayment --> PaymentCompleted : Payment received
  PaymentCompleted --> [*]
}

Confirmed --> Preparing : Kitchen starts preparation\n[payment completed]
Confirmed --> Cancelled : Customer cancels\n[cancellation allowed]

Preparing : Entry: Send "Chef is preparing" notification
Preparing : Entry: Update kitchen job = IN_PROGRESS
Preparing : Kitchen staff preparing meal
Preparing : Inventory being used

Preparing --> Ready : Preparation completed\n[kitchen job done]
Preparing --> Cancelled : Cannot complete\n[ingredient unavailable]

Ready : Entry: Send "Ready for pickup" notification
Ready : Entry: Update kitchen job = COMPLETED
Ready : Waiting for customer pickup
Ready : Order packaged

Ready --> Completed : Customer picks up\n[staff confirms]
Ready --> Cancelled : Customer no-show\n[after 1 hour timeout]

Completed : Entry: Send "Enjoy meal" notification
Completed : Entry: Deduct inventory stock
Completed : Entry: Record completion time
Completed : Can be rated by customer
Completed : Final state (terminal)

Cancelled : Entry: Send cancellation notification
Cancelled : Entry: Cancel kitchen job (if exists)
Cancelled : Entry: Initiate refund (if paid)
Cancelled : Reason recorded
Cancelled : Final state (terminal)

Pending --> Cancelled : Auto-cancel\n[no action after 15 mins]

note right of Pending
  - Initial state after creation
  - subtotalAmount, totalAmount calculated
  - Can apply promotion code
  - Can cancel freely
end note

note right of Confirmed
  - Cannot be cancelled after kitchen starts
  - Payment processing
  - Kitchen job created
end note

note right of Preparing
  - Active kitchen job
  - Staff working on order
  - Inventory being reserved
end note

note right of Ready
  - Order packaged and ready
  - Customer notified
  - 1 hour pickup window
end note

note right of Completed
  - Terminal state
  - Inventory deducted
  - Can receive rating
  - Payment finalized
end note

note right of Cancelled
  - Terminal state
  - Cancellation reason recorded
  - Refund processed if applicable
  - Inventory released
end note

@enduml
```

## 3. Payment Transaction State Diagram

```plantuml
@startuml
title Payment Transaction State Machine

[*] --> Pending : Create payment

Pending : Entry: Generate transactionId
Pending : Entry: Create payment record
Pending : Entry: Build ZaloPay request
Pending : Waiting for payment URL

Pending --> Initiated : ZaloPay order created\n[payment URL received]

Initiated : Entry: Save zpTransToken
Initiated : Payment URL sent to customer
Initiated : Customer redirected to ZaloPay

Initiated --> Processing : Customer submits payment\n[in ZaloPay app]

Processing : Entry: Customer authenticated
Processing : ZaloPay processing payment
Processing : Validating payment method
Processing : Checking account balance

Processing --> Completed : Payment successful\n[callback received]
Processing --> Failed : Payment failed\n[insufficient funds / error]
Processing --> Expired : No action taken\n[timeout after 15 mins]

Completed : Entry: Receive success callback
Completed : Entry: Verify callback MAC
Completed : Entry: Save zpTransId
Completed : Entry: Set completedAt timestamp
Completed : Entry: Update order paymentStatus = PAID
Completed : Final state (terminal)

Failed : Entry: Receive failure callback
Failed : Entry: Save failure reason
Failed : Customer can retry payment
Failed : Order remains unpaid

Failed --> Pending : Retry payment\n[create new transaction]

Expired : Entry: ZaloPay order expired
Expired : No callback received
Expired : Customer can retry

Expired --> Pending : Retry payment\n[create new transaction]

state RefundProcess {
  [*] --> RefundPending : Initiate refund\n[order cancelled after payment]
  
  RefundPending : Entry: Create refund transaction
  RefundPending : Entry: Call ZaloPay refund API
  RefundPending : Waiting for refund processing
  
  RefundPending --> RefundCompleted : Refund successful\n[3-5 business days]
  RefundPending --> RefundFailed : Refund failed\n[technical error]
  
  RefundCompleted : Entry: Update refund status
  RefundCompleted : Entry: Notify customer
  RefundCompleted : Funds returned to customer
  
  RefundFailed : Entry: Log error
  RefundFailed : Entry: Alert admin
  RefundFailed : Requires manual intervention
}

Completed --> RefundProcess : Order cancelled\n[after payment]

RefundProcess --> Refunded : Refund completed

Refunded : Final state (terminal)
Refunded : Money returned to customer
Refunded : Transaction closed

note right of Pending
  - Initial state
  - transactionId generated (app_trans_id)
  - Ready to create ZaloPay order
end note

note right of Initiated
  - ZaloPay order created
  - zpTransToken received
  - Payment URL ready for customer
end note

note right of Processing
  - Customer in ZaloPay flow
  - Payment being processed
  - Awaiting callback
end note

note right of Completed
  - Terminal state (success)
  - Callback MAC verified
  - Order marked as paid
  - Cannot be reversed (only refunded)
end note

note right of Failed
  - Can retry with new transaction
  - Original transaction remains failed
  - Failure reason recorded
end note

note right of Refunded
  - Terminal state
  - Original payment reversed
  - Customer money returned
end note

@enduml
```

## 4. Kitchen Job State Diagram

```plantuml
@startuml
title Kitchen Job State Machine

[*] --> Pending : Order confirmed

Pending : Entry: Create job record
Pending : Entry: Link to order
Pending : Entry: List required ingredients
Pending : Waiting for staff assignment
Pending : In kitchen queue

Pending --> Assigned : Staff self-assigns\n[staff selects job]

Assigned : Entry: Set assignedTo staff
Assigned : Entry: Set assignedAt timestamp
Assigned : Staff preparing to start
Assigned : Reviewing order details

Assigned --> InProgress : Staff starts preparation\n[begins cooking]

InProgress : Entry: Set startedAt timestamp
InProgress : Entry: Notify customer "Preparing"
InProgress : Staff actively cooking
InProgress : Ingredients being used
InProgress : Following recipe steps

InProgress --> OnHold : Issue encountered\n[missing ingredient / equipment problem]

OnHold : Entry: Set holdReason
OnHold : Entry: Notify manager
OnHold : Temporary pause
OnHold : Waiting for resolution

OnHold --> InProgress : Issue resolved\n[ingredient restocked / equipment fixed]
OnHold --> Cancelled : Cannot resolve\n[critical issue]

InProgress --> QualityCheck : Preparation complete\n[meal ready]

QualityCheck : Entry: Staff reviews quality
QualityCheck : Checking presentation
QualityCheck : Verifying ingredients
QualityCheck : Taste check (if applicable)

QualityCheck --> InProgress : Quality issue\n[needs remake]
QualityCheck --> Completed : Quality approved\n[ready to serve]

Completed : Entry: Set completedAt timestamp
Completed : Entry: Calculate preparation time
Completed : Entry: Update order status = READY
Completed : Entry: Notify customer "Ready for pickup"
Completed : Job finished successfully
Completed : Final state (terminal)

Pending --> Cancelled : Order cancelled\n[before assignment]
Assigned --> Cancelled : Order cancelled\n[before start]
InProgress --> Cancelled : Order cancelled\n[customer request / critical issue]

Cancelled : Entry: Set cancelledAt timestamp
Cancelled : Entry: Set cancellation reason
Cancelled : Entry: Release assigned staff
Cancelled : Job terminated
Cancelled : Final state (terminal)

Pending --> Expired : No action taken\n[timeout after 30 mins]

Expired : Entry: Set expiredAt timestamp
Expired : Entry: Alert kitchen manager
Expired : Order still in system but flagged
Expired : Requires manager action

note right of Pending
  - Newly created job
  - Visible in kitchen dashboard
  - Waiting for staff to pick up
end note

note right of Assigned
  - Staff member assigned
  - Staff can view full details
  - Can be reassigned by manager
end note

note right of InProgress
  - Active preparation
  - Timer running
  - Staff cannot abandon
end note

note right of OnHold
  - Temporary state
  - Doesn't stop other jobs
  - Manager alerted
  - Must be resolved or cancelled
end note

note right of QualityCheck
  - Quality assurance step
  - Ensures customer satisfaction
  - Failed check = remake
end note

note right of Completed
  - Terminal state (success)
  - Preparation time recorded
  - Used for analytics
  - Order ready for pickup
end note

note right of Cancelled
  - Terminal state (failure)
  - Reason documented
  - May trigger refund
  - Staff freed for next job
end note

@enduml
```

## 5. Promotion State Diagram

```plantuml
@startuml
title Promotion State Machine

[*] --> Draft : Create promotion

Draft : Entry: Initial creation
Draft : Admin setting parameters
Draft : Not visible to customers
Draft : Can be freely edited
Draft : No validations yet

Draft --> Scheduled : Activate & schedule\n[set validFrom future date]
Draft --> Active : Activate immediately\n[validFrom <= now <= validUntil]

Scheduled : Entry: Set isActive = true
Scheduled : Entry: Visible in system but not usable
Scheduled : Waiting for validFrom date
Scheduled : Can be edited

Scheduled --> Active : validFrom date reached\n[now >= validFrom]
Scheduled --> Cancelled : Admin cancels\n[before activation]

Active : Entry: Visible to customers
Active : Entry: Can be redeemed
Active : Customers can apply code
Active : Usage count incrementing
Active : Within valid date range

Active --> Active : Customer redeems\n[increment usageCount]

Active --> UsageLimitReached : All uses exhausted\n[currentUsage >= usageLimit]
Active --> Expired : validUntil date passed\n[now > validUntil]
Active --> Suspended : Admin suspends\n[temporary pause]

UsageLimitReached : Entry: Set isActive = false
UsageLimitReached : No more redemptions allowed
UsageLimitReached : Code still exists in system
UsageLimitReached : Cannot be reactivated

Suspended : Entry: Set isActive = false
Suspended : Temporarily unavailable
Suspended : Can be reactivated
Suspended : Existing redemptions not affected

Suspended --> Active : Admin reactivates\n[and within valid dates]
Suspended --> Expired : validUntil passed while suspended

Expired : Entry: Set isActive = false
Expired : Entry: Set expiredAt timestamp
Expired : validUntil date passed
Expired : Can be viewed in history
Expired : Cannot be redeemed
Expired : Final state (terminal)

Draft --> Cancelled : Admin deletes\n[before activation]
Scheduled --> Cancelled : Admin cancels\n[before going active]
Active --> Cancelled : Admin force-cancels\n[emergency termination]
Suspended --> Cancelled : Admin deletes

Cancelled : Entry: Set isActive = false
Cancelled : Entry: Set cancelledAt timestamp
Cancelled : Entry: Set cancellation reason
Cancelled : Removed from customer view
Cancelled : Existing redemptions honored
Cancelled : Final state (terminal)

state "Validation Rules" as Validation {
  [*] --> CheckDates
  CheckDates : validFrom < validUntil
  CheckDates : validFrom <= now (for immediate activation)
  
  CheckDates --> CheckDiscount
  CheckDiscount : If PERCENT_OFF: 0 < percentOff <= 100
  CheckDiscount : If FIXED_AMOUNT_OFF: fixedAmount > 0
  CheckDiscount : maxDiscountAmount >= 0 (for PERCENT_OFF)
  
  CheckDiscount --> CheckUsage
  CheckUsage : usageLimit > 0
  CheckUsage : minOrderValue >= 0
  
  CheckUsage --> CheckCode
  CheckCode : Code unique (case-insensitive)
  CheckCode : Code length 3-50 chars
  
  CheckCode --> [*] : All valid
}

Draft --> Validation : Before activation
Scheduled --> Validation : On edit
Suspended --> Validation : Before reactivation

note right of Draft
  - Initial creation state
  - Can be edited freely
  - No impact on customers
  - Must pass validation to activate
end note

note right of Scheduled
  - Activated but not yet valid
  - Waiting for validFrom date
  - Visible in admin panel
  - Can still be edited
end note

note right of Active
  - Fully functional
  - Customers can use code
  - Usage count tracked
  - Automatically expires at validUntil
end note

note right of UsageLimitReached
  - Terminal state (success)
  - All uses exhausted
  - Cannot be reactivated
  - Promotion was successful
end note

note right of Suspended
  - Temporary pause state
  - Admin can reactivate
  - Useful for adjustments
  - Doesn't affect past redemptions
end note

note right of Expired
  - Terminal state (natural end)
  - validUntil date passed
  - Kept for historical records
  - Cannot be reused
end note

note right of Cancelled
  - Terminal state (admin action)
  - Force-terminated by admin
  - Reason documented
  - Past redemptions still valid
end note

@enduml
```

## 6. Notification State Diagram

```plantuml
@startuml
title Notification State Machine

[*] --> Created : Create notification

Created : Entry: Generate notification record
Created : Entry: Set notification content
Created : Entry: Link to user and order (if applicable)
Created : Ready to be sent
Created : sentAt = null

Created --> Sending : Send via FCM

Sending : Entry: Build FCM message
Sending : Entry: Get user FCM token
Sending : Calling Firebase API
Sending : Waiting for response

Sending --> Sent : FCM API success\n[message ID received]
Sending --> Failed : FCM API error\n[no FCM token / network error / invalid token]

Sent : Entry: Set sentAt = NOW()
Sent : Entry: Save FCM message ID
Sent : Notification delivered to device
Sent : Waiting for user action
Sent : readAt = null

Failed : Entry: Log error details
Failed : Entry: Set failureReason
Failed : Not delivered to device
Failed : Can retry if transient error

Failed --> Sending : Retry send\n[transient error / token refreshed]
Failed --> Abandoned : Max retries reached\n[3 attempts]

Abandoned : Entry: Mark as permanently failed
Abandoned : No more retry attempts
Abandoned : Logged for audit
Abandoned : Final state (terminal)

Sent --> Delivered : Device confirms delivery\n[FCM delivery receipt]

Delivered : Entry: Set deliveredAt timestamp
Delivered : Confirmed on user device
Delivered : May be in notification tray
Delivered : Still unread

Delivered --> Opened : User taps notification\n[opens app]

Opened : Entry: Set readAt = NOW()
Opened : User viewed content
Opened : Deep link executed (if applicable)
Opened : Navigated to relevant screen

Opened --> Actioned : User performs action\n[e.g., views order details]

Actioned : Entry: Set actionedAt timestamp
Actioned : User completed intended action
Actioned : Notification served its purpose
Actioned : Final state (terminal)

Delivered --> Read : User marks as read\n[without opening]

Read : Entry: Set readAt = NOW()
Read : User acknowledged notification
Read : May not have performed action
Read : Terminal state

Delivered --> Dismissed : User dismisses\n[swipes away]

Dismissed : Entry: Set dismissedAt timestamp
Dismissed : User ignored notification
Dismissed : Did not open or read
Dismissed : Terminal state

Sent --> Expired : Not opened\n[after 7 days]

Expired : Entry: Set expiredAt timestamp
Expired : Notification too old
Expired : Still in database for history
Expired : Terminal state

state "Notification Types" as Types {
  state ORDER_UPDATE
  ORDER_UPDATE : Order status changed
  ORDER_UPDATE : Links to order details
  ORDER_UPDATE : High priority
  
  state PROMOTION
  PROMOTION : Marketing message
  PROMOTION : Links to promotion
  PROMOTION : Medium priority
  
  state SYSTEM
  SYSTEM : System announcements
  SYSTEM : No specific link
  SYSTEM : Low priority
}

Created --> Types : Categorize

note right of Created
  - Initial state
  - Notification prepared
  - Not yet sent
  - Can be cancelled before sending
end note

note right of Sending
  - Transient state
  - FCM API call in progress
  - Usually quick (< 1 second)
end note

note right of Sent
  - Sent to FCM successfully
  - Doesn't mean delivered to device
  - Device may be offline
end note

note right of Failed
  - Send failed
  - Common reasons:
    * No FCM token
    * Invalid/expired token
    * Network error
  - Can retry
end note

note right of Delivered
  - Confirmed on device
  - In notification tray
  - User hasn't interacted yet
end note

note right of Opened
  - User tapped notification
  - App opened to relevant screen
  - readAt timestamp set
end note

note right of Actioned
  - Terminal state (success)
  - User completed intended action
  - Notification was effective
end note

note right of Read
  - Terminal state
  - User acknowledged
  - May not have acted
end note

note right of Dismissed
  - Terminal state
  - User ignored notification
  - Analytics: low engagement
end note

note right of Expired
  - Terminal state
  - Notification too old
  - Still in history
end note

@enduml
```


