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

[*] --> PENDING : Create order

state PENDING <<Pending>>
state CONFIRMED <<Confirmed>>
state PREPARING <<Preparing>>
state READY <<Ready>>
state COMPLETED <<Completed>>
state CANCELLED <<Cancelled>>

PENDING --> CONFIRMED : Admin confirms order
PENDING --> CANCELLED : Customer/Admin cancels

CONFIRMED --> PREPARING : Kitchen starts preparation
CONFIRMED --> CANCELLED : Customer cancels

PREPARING --> READY : Preparation completed
PREPARING --> CANCELLED : Cannot complete

READY --> COMPLETED : Customer picks up order
READY --> CANCELLED : Customer no-show (timeout)

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

[*] --> PENDING : Create payment

state PENDING <<Pending>>
state INITIATED <<Initiated>>
state PROCESSING <<Processing>>
state COMPLETED <<Completed>>
state FAILED <<Failed>>
state EXPIRED <<Failed>>
state REFUNDED <<Refunded>>

PENDING --> INITIATED : ZaloPay order created

INITIATED --> PROCESSING : Customer submits payment
INITIATED --> EXPIRED : Timeout (15 minutes)

PROCESSING --> COMPLETED : Payment successful
PROCESSING --> FAILED : Payment failed

FAILED --> PENDING : Retry payment
EXPIRED --> PENDING : Retry payment

COMPLETED --> REFUNDED : Order cancelled (refund)

COMPLETED --> [*]
REFUNDED --> [*]


@enduml
```

---

## State Transition Rules

### Order State Transitions

| From State | To State | Trigger |
|------------|----------|---------|
| PENDING | CONFIRMED | Admin confirms |
| PENDING | CANCELLED | User/Admin cancels |
| CONFIRMED | PREPARING | Kitchen starts |
| PREPARING | READY | Preparation done |
| READY | COMPLETED | Customer pickup |
| Any State | CANCELLED | Special conditions |

### Payment State Transitions

| From State | To State | Trigger |
|------------|----------|---------|
| PENDING | INITIATED | ZaloPay order created |
| INITIATED | PROCESSING | Customer submits payment |
| PROCESSING | COMPLETED | Payment successful |
| PROCESSING | FAILED | Payment failed |
| COMPLETED | REFUNDED | Order cancelled (refund) |

