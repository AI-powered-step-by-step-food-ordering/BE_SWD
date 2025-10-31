# Use Case Diagram - Healthy Food Ordering System

## PlantUML Code

```plantuml
@startuml
left to right direction
skinparam packageStyle rectangle

actor "Customer\n(USER)" as Customer
actor "Admin" as Admin
actor "Kitchen Staff\n(STAFF)" as Staff
actor "ZaloPay Gateway" as ZaloPay
actor "Firebase FCM" as FCM

rectangle "Healthy Food Ordering System" {
  
  package "Authentication & User Management" {
    usecase "Register Account" as UC1
    usecase "Login" as UC2
    usecase "Logout" as UC3
    usecase "Verify Email (OTP)" as UC4
    usecase "Forgot Password" as UC5
    usecase "Reset Password" as UC6
    usecase "Update Profile" as UC7
    usecase "Manage FCM Token" as UC8
  }
  
  package "Bowl & Order Management" {
    usecase "Browse Bowl Templates" as UC9
    usecase "Create Custom Bowl" as UC10
    usecase "Add Ingredients to Bowl" as UC11
    usecase "View Ingredient Details" as UC12
    usecase "Create Order" as UC13
    usecase "View Order History" as UC14
    usecase "View Order Details" as UC15
    usecase "Cancel Order" as UC16
    usecase "Apply Promotion Code" as UC17
  }
  
  package "Payment Management" {
    usecase "Pay with ZaloPay" as UC18
    usecase "View Payment Status" as UC19
    usecase "Process Payment Callback" as UC20
  }
  
  package "Notification Management" {
    usecase "Receive Order Notifications" as UC21
    usecase "View Notification History" as UC22
    usecase "Mark Notification as Read" as UC23
    usecase "Send Promotional Notifications" as UC24
  }
  
  package "Admin Management" {
    usecase "Manage Users" as UC25
    usecase "Manage Categories" as UC26
    usecase "Manage Ingredients" as UC27
    usecase "Manage Promotions" as UC28
    usecase "Manage Stores" as UC29
    usecase "View All Orders" as UC30
    usecase "Update Order Status" as UC31
  }
  
  package "Kitchen Management" {
    usecase "View Kitchen Jobs" as UC32
    usecase "Update Job Status" as UC33
    usecase "Manage Inventory" as UC34
    usecase "Track Stock Levels" as UC35
  }
}

' Customer relationships
Customer --> UC1
Customer --> UC2
Customer --> UC3
Customer --> UC4
Customer --> UC5
Customer --> UC6
Customer --> UC7
Customer --> UC8
Customer --> UC9
Customer --> UC10
Customer --> UC11
Customer --> UC12
Customer --> UC13
Customer --> UC14
Customer --> UC15
Customer --> UC16
Customer --> UC17
Customer --> UC18
Customer --> UC19
Customer --> UC21
Customer --> UC22
Customer --> UC23

' Admin relationships
Admin --> UC25
Admin --> UC26
Admin --> UC27
Admin --> UC28
Admin --> UC29
Admin --> UC30
Admin --> UC31
Admin --> UC24

' Kitchen Staff relationships
Staff --> UC32
Staff --> UC33
Staff --> UC34
Staff --> UC35
Staff --> UC31

' System interactions
UC18 ..> ZaloPay : <<uses>>
UC20 ..> ZaloPay : <<uses>>
UC21 ..> FCM : <<uses>>
UC24 ..> FCM : <<uses>>

' Include relationships
UC13 ..> UC17 : <<include>>
UC31 ..> UC21 : <<include>>
UC2 ..> UC8 : <<include>>
UC3 ..> UC8 : <<include>>

' Extend relationships
UC10 ..> UC11 : <<extend>>
UC13 ..> UC18 : <<extend>>

@enduml
```

## Use Case Descriptions

### Customer Use Cases

1. **Register Account (UC1)**: Customer creates a new account with email and password
2. **Login (UC2)**: Customer logs in with email and password
3. **Logout (UC3)**: Customer logs out and removes FCM token
4. **Verify Email (UC4)**: Customer verifies email with 6-digit OTP
5. **Forgot Password (UC5)**: Customer requests password reset OTP via email
6. **Reset Password (UC6)**: Customer resets password using OTP
7. **Update Profile (UC7)**: Customer updates personal information
8. **Manage FCM Token (UC8)**: System saves/removes FCM token for push notifications
9. **Browse Bowl Templates (UC9)**: Customer views available bowl templates
10. **Create Custom Bowl (UC10)**: Customer creates a custom bowl from template
11. **Add Ingredients to Bowl (UC11)**: Customer adds/removes ingredients
12. **View Ingredient Details (UC12)**: Customer views ingredient nutritional info
13. **Create Order (UC13)**: Customer places an order with bowls
14. **View Order History (UC14)**: Customer views past orders
15. **View Order Details (UC15)**: Customer views specific order details
16. **Cancel Order (UC16)**: Customer cancels a pending order
17. **Apply Promotion Code (UC17)**: Customer applies discount code to order
18. **Pay with ZaloPay (UC18)**: Customer pays using ZaloPay gateway
19. **View Payment Status (UC19)**: Customer checks payment transaction status
20. **Receive Order Notifications (UC21)**: Customer receives push notifications for order updates
21. **View Notification History (UC22)**: Customer views all notifications
22. **Mark Notification as Read (UC23)**: Customer marks notification as read

### Admin Use Cases

24. **Send Promotional Notifications (UC24)**: Admin sends promotional push notifications
25. **Manage Users (UC25)**: Admin creates/updates/deletes user accounts
26. **Manage Categories (UC26)**: Admin manages ingredient categories
27. **Manage Ingredients (UC27)**: Admin adds/updates ingredients and prices
28. **Manage Promotions (UC28)**: Admin creates/updates promotion codes
29. **Manage Stores (UC29)**: Admin manages store locations
30. **View All Orders (UC30)**: Admin views all orders in system
31. **Update Order Status (UC31)**: Admin/Staff updates order status (triggers notification)

### Kitchen Staff Use Cases

32. **View Kitchen Jobs (UC32)**: Staff views assigned preparation jobs
33. **Update Job Status (UC33)**: Staff updates job completion status
34. **Manage Inventory (UC34)**: Staff adds/removes ingredient stock
35. **Track Stock Levels (UC35)**: Staff monitors ingredient availability

## Actors Description

- **Customer (USER)**: End-user who orders food
- **Admin**: System administrator with full access
- **Kitchen Staff (STAFF)**: Restaurant staff managing orders and inventory
- **ZaloPay Gateway**: External payment processing system
- **Firebase FCM**: External push notification service


