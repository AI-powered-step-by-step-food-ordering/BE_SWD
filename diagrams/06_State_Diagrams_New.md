# State Diagrams - Simplified

## 1. Order State Machine

```plantuml
@startuml
skinparam state {
  BackgroundColor<<Pending>> #FFF4E6
  BackgroundColor<<Confirmed>> #E3F2FD
  BackgroundColor<<Preparing>> #FFF9C4
  BackgroundColor<<Ready>> #C8E6C9
  BackgroundColor<<Completed>> #A5D6A7
  BackgroundColor<<Cancelled>> #FFCDD2
  BorderColor DarkSlateGray
  FontName Arial
}

title Order Status State Machine

[*] --> PENDING

state PENDING <<Pending>>
state CONFIRMED <<Confirmed>>
state PREPARING <<Preparing>>
state READY <<Ready>>
state COMPLETED <<Completed>>
state CANCELLED <<Cancelled>>

PENDING --> CONFIRMED
PENDING --> CANCELLED

CONFIRMED --> PREPARING
CONFIRMED --> CANCELLED

PREPARING --> READY
PREPARING --> CANCELLED

READY --> COMPLETED
READY --> CANCELLED

COMPLETED --> [*]
CANCELLED --> [*]

@enduml
```

---

## 2. Payment Transaction State Machine

```plantuml
@startuml
skinparam state {
  BackgroundColor<<Pending>> #FFF4E6
  BackgroundColor<<Initiated>> #E3F2FD
  BackgroundColor<<Processing>> #BBDEFB
  BackgroundColor<<Completed>> #C8E6C9
  BackgroundColor<<Failed>> #FFCDD2
  BackgroundColor<<Refunded>> #F0F0F0
  BorderColor DarkSlateGray
  FontName Arial
}

title Payment Transaction State Machine

[*] --> PENDING

state PENDING <<Pending>>
state INITIATED <<Initiated>>
state PROCESSING <<Processing>>
state COMPLETED <<Completed>>
state FAILED <<Failed>>
state EXPIRED <<Failed>>
state REFUNDED <<Refunded>>

PENDING --> INITIATED
INITIATED --> PROCESSING
INITIATED --> EXPIRED

PROCESSING --> COMPLETED
PROCESSING --> FAILED

FAILED --> PENDING
EXPIRED --> PENDING

COMPLETED --> REFUNDED
COMPLETED --> [*]
REFUNDED --> [*]

@enduml
```

---

## 3. User Account State Machine

```plantuml
@startuml
skinparam state {
  BackgroundColor<<Active>> #C8E6C9
  BackgroundColor<<Inactive>> #FFF4E6
  BackgroundColor<<Suspended>> #FFCDD2
  BackgroundColor<<Locked>> #FF8A80
  BackgroundColor<<Deleted>> #BDBDBD
  BorderColor DarkSlateGray
  FontName Arial
}

title User Account State Machine

[*] --> INACTIVE

state INACTIVE <<Inactive>>
state ACTIVE <<Active>>
state SUSPENDED <<Suspended>>
state LOCKED <<Locked>>
state DELETED <<Deleted>>

INACTIVE --> ACTIVE
ACTIVE --> SUSPENDED
ACTIVE --> LOCKED
ACTIVE --> DELETED

SUSPENDED --> ACTIVE
LOCKED --> ACTIVE

SUSPENDED --> DELETED
LOCKED --> DELETED

DELETED --> [*]

@enduml
```

---

## 4. Ingredient Stock State Machine

```plantuml
@startuml
skinparam state {
  BackgroundColor<<Available>> #C8E6C9
  BackgroundColor<<Low>> #FFF9C4
  BackgroundColor<<Out>> #FFCDD2
  BackgroundColor<<Reordering>> #E3F2FD
  BackgroundColor<<Discontinued>> #BDBDBD
  BorderColor DarkSlateGray
  FontName Arial
}

title Ingredient Stock State Machine

[*] --> AVAILABLE

state AVAILABLE <<Available>>
state LOW_STOCK <<Low>>
state OUT_OF_STOCK <<Out>>
state REORDERING <<Reordering>>
state DISCONTINUED <<Discontinued>>

AVAILABLE --> LOW_STOCK
LOW_STOCK --> AVAILABLE
LOW_STOCK --> OUT_OF_STOCK
LOW_STOCK --> REORDERING

OUT_OF_STOCK --> REORDERING
REORDERING --> AVAILABLE

AVAILABLE --> DISCONTINUED
LOW_STOCK --> DISCONTINUED
OUT_OF_STOCK --> DISCONTINUED

DISCONTINUED --> [*]

@enduml
```

---

## 5. Promotion State Machine

```plantuml
@startuml
skinparam state {
  BackgroundColor<<Draft>> #F5F5F5
  BackgroundColor<<Scheduled>> #E3F2FD
  BackgroundColor<<Active>> #C8E6C9
  BackgroundColor<<Paused>> #FFF9C4
  BackgroundColor<<Expired>> #FFCDD2
  BackgroundColor<<Archived>> #BDBDBD
  BorderColor DarkSlateGray
  FontName Arial
}

title Promotion State Machine

[*] --> DRAFT

state DRAFT <<Draft>>
state SCHEDULED <<Scheduled>>
state ACTIVE <<Active>>
state PAUSED <<Paused>>
state EXPIRED <<Expired>>
state ARCHIVED <<Archived>>

DRAFT --> SCHEDULED
DRAFT --> ACTIVE

SCHEDULED --> ACTIVE
SCHEDULED --> ARCHIVED

ACTIVE --> PAUSED
ACTIVE --> EXPIRED

PAUSED --> ACTIVE
PAUSED --> EXPIRED

EXPIRED --> ARCHIVED
ARCHIVED --> [*]

@enduml
```

---

## 6. Bowl Customization State Machine

```plantuml
@startuml
skinparam state {
  BackgroundColor<<Creating>> #FFF4E6
  BackgroundColor<<Valid>> #C8E6C9
  BackgroundColor<<Invalid>> #FFCDD2
  BackgroundColor<<Saved>> #E3F2FD
  BackgroundColor<<Ordered>> #A5D6A7
  BorderColor DarkSlateGray
  FontName Arial
}

title Bowl Customization State Machine

[*] --> CREATING

state CREATING <<Creating>>
state VALID <<Valid>>
state INVALID <<Invalid>>
state SAVED_TO_CART <<Saved>>
state ORDERED <<Ordered>>

CREATING --> VALID
CREATING --> INVALID

INVALID --> CREATING
INVALID --> VALID

VALID --> SAVED_TO_CART
SAVED_TO_CART --> VALID
SAVED_TO_CART --> ORDERED

ORDERED --> [*]

@enduml
```

---

## 7. Notification Delivery State Machine

```plantuml
@startuml
skinparam state {
  BackgroundColor<<Pending>> #FFF4E6
  BackgroundColor<<Sent>> #C8E6C9
  BackgroundColor<<Delivered>> #A5D6A7
  BackgroundColor<<Read>> #E3F2FD
  BackgroundColor<<Failed>> #FFCDD2
  BorderColor DarkSlateGray
  FontName Arial
}

title Notification Delivery State Machine

[*] --> PENDING

state PENDING <<Pending>>
state SENT <<Sent>>
state DELIVERED <<Delivered>>
state READ <<Read>>
state FAILED <<Failed>>
state RETRY <<Pending>>

PENDING --> SENT
PENDING --> FAILED

SENT --> DELIVERED
SENT --> FAILED

DELIVERED --> READ
FAILED --> RETRY

RETRY --> SENT
RETRY --> FAILED

READ --> [*]
FAILED --> [*]

@enduml
```

---

## 8. Bowl Template State Machine

```plantuml
@startuml
skinparam state {
  BackgroundColor<<Draft>> #FFF4E6
  BackgroundColor<<Active>> #C8E6C9
  BackgroundColor<<Inactive>> #F5F5F5
  BackgroundColor<<Archived>> #BDBDBD
  BorderColor DarkSlateGray
  FontName Arial
}

title Bowl Template State Machine

[*] --> DRAFT

state DRAFT <<Draft>>
state ACTIVE <<Active>>
state INACTIVE <<Inactive>>
state ARCHIVED <<Archived>>

DRAFT --> ACTIVE
DRAFT --> ARCHIVED

ACTIVE --> INACTIVE
INACTIVE --> ACTIVE

ACTIVE --> ARCHIVED
INACTIVE --> ARCHIVED

ARCHIVED --> [*]

@enduml
```

---

## 9. OTP Verification State Machine

```plantuml
@startuml
skinparam state {
  BackgroundColor<<Generated>> #E3F2FD
  BackgroundColor<<Sent>> #BBDEFB
  BackgroundColor<<Verified>> #C8E6C9
  BackgroundColor<<Expired>> #FFCDD2
  BackgroundColor<<Invalid>> #FF8A80
  BorderColor DarkSlateGray
  FontName Arial
}

title OTP Verification State Machine

[*] --> GENERATED

state GENERATED <<Generated>>
state SENT <<Sent>>
state VERIFIED <<Verified>>
state EXPIRED <<Expired>>
state INVALID_ATTEMPT <<Invalid>>

GENERATED --> SENT
SENT --> VERIFIED
SENT --> EXPIRED
SENT --> INVALID_ATTEMPT

INVALID_ATTEMPT --> SENT
INVALID_ATTEMPT --> EXPIRED

VERIFIED --> [*]
EXPIRED --> [*]

@enduml
```

---

## 10. Ingredient Category State Machine

```plantuml
@startuml
skinparam state {
  BackgroundColor<<Active>> #C8E6C9
  BackgroundColor<<Inactive>> #FFF4E6
  BackgroundColor<<Hidden>> #F5F5F5
  BackgroundColor<<Deprecated>> #BDBDBD
  BorderColor DarkSlateGray
  FontName Arial
}

title Ingredient Category State Machine

[*] --> ACTIVE

state ACTIVE <<Active>>
state INACTIVE <<Inactive>>
state HIDDEN <<Hidden>>
state DEPRECATED <<Deprecated>>

ACTIVE --> INACTIVE
ACTIVE --> HIDDEN

INACTIVE --> ACTIVE
INACTIVE --> DEPRECATED

HIDDEN --> ACTIVE
HIDDEN --> DEPRECATED

DEPRECATED --> [*]

@enduml
```

---

## 11. Promotion Redemption State Machine

```plantuml
@startuml
skinparam state {
  BackgroundColor<<Applied>> #C8E6C9
  BackgroundColor<<Pending>> #FFF9C4
  BackgroundColor<<Voided>> #FFCDD2
  BackgroundColor<<Refunded>> #E3F2FD
  BorderColor DarkSlateGray
  FontName Arial
}

title Promotion Redemption State Machine

[*] --> PENDING

state PENDING <<Pending>>
state APPLIED <<Applied>>
state VOIDED <<Voided>>
state REFUNDED <<Refunded>>

PENDING --> APPLIED
PENDING --> VOIDED

APPLIED --> VOIDED
APPLIED --> REFUNDED

VOIDED --> [*]
REFUNDED --> [*]

@enduml
```

---

## 12. AI Bowl Analysis State Machine

```plantuml
@startuml
skinparam state {
  BackgroundColor<<Requested>> #E3F2FD
  BackgroundColor<<Processing>> #BBDEFB
  BackgroundColor<<Completed>> #C8E6C9
  BackgroundColor<<Failed>> #FFCDD2
  BackgroundColor<<Cached>> #FFF9C4
  BorderColor DarkSlateGray
  FontName Arial
}

title AI Bowl Analysis State Machine

[*] --> REQUESTED

state REQUESTED <<Requested>>
state PROCESSING <<Processing>>
state COMPLETED <<Completed>>
state FAILED <<Failed>>
state CACHED <<Cached>>

REQUESTED --> PROCESSING
REQUESTED --> CACHED

PROCESSING --> COMPLETED
PROCESSING --> FAILED

COMPLETED --> CACHED

CACHED --> REQUESTED
FAILED --> REQUESTED

COMPLETED --> [*]

@enduml
```

---

## State Transition Summary

All state diagrams follow a simplified approach without transition labels for cleaner visualization. Each state machine represents the possible states and transitions for different entities in the Healthy Food Ordering System.

**Diagrams included:**
1. Order Status - Complete order lifecycle
2. Payment Transaction - Payment processing flow
3. User Account - Account status management
4. Ingredient Stock - Inventory tracking
5. Promotion - Marketing campaign lifecycle
6. Bowl Customization - Bowl building process
7. Notification Delivery - Push notification states
8. Bowl Template - Template management lifecycle
9. OTP Verification - Email/SMS verification flow
10. Ingredient Category - Category management
11. Promotion Redemption - Promotion usage tracking
12. AI Bowl Analysis - AI analysis request flow

