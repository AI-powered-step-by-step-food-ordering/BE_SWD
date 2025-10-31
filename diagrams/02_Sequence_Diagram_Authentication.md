# Sequence Diagrams - Authentication Flows

## 1. User Registration and Email Verification

```plantuml
@startuml
title User Registration and Email Verification Flow

actor Customer
participant "AuthController" as Controller
participant "AuthService" as AuthService
participant "UserService" as UserService
participant "OtpService" as OtpService
participant "EmailService" as EmailService
participant "Database" as DB

== Registration ==
Customer -> Controller: POST /api/auth/register\n{email, password, fullName}
activate Controller
Controller -> AuthService: register(RegisterRequest)
activate AuthService

AuthService -> UserService: findByEmail(email)
activate UserService
UserService -> DB: SELECT * FROM users WHERE email=?
DB --> UserService: null (email not exists)
deactivate UserService

AuthService -> OtpService: generateOtp()
activate OtpService
OtpService --> AuthService: "123456"
deactivate OtpService

AuthService -> UserService: create(User)
activate UserService
UserService -> DB: INSERT INTO users\n(email, passwordHash, status=PENDING_VERIFICATION,\nemailVerificationOtp, otpExpiry)
DB --> UserService: User created
deactivate UserService

AuthService -> EmailService: sendVerificationOtp(email, otp)
activate EmailService
EmailService -> EmailService: Load HTML template
EmailService -> EmailService: Send email via SMTP
EmailService --> AuthService: Email sent
deactivate EmailService

AuthService --> Controller: EmailVerificationResponse\n{email, otpSentAt, expiresIn}
deactivate AuthService
Controller --> Customer: 200 OK\n"OTP sent to email"
deactivate Controller

== Email Verification ==
Customer -> Controller: POST /api/auth/verify-otp\n{email, otp}
activate Controller
Controller -> AuthService: verifyOtp(VerifyOtpRequest)
activate AuthService

AuthService -> UserService: findByEmail(email)
activate UserService
UserService -> DB: SELECT * FROM users WHERE email=?
DB --> UserService: User with OTP
deactivate UserService

AuthService -> OtpService: validateOtp(user, otp)
activate OtpService
OtpService -> OtpService: Check otp match
OtpService -> OtpService: Check expiry time
OtpService --> AuthService: Valid
deactivate OtpService

AuthService -> UserService: update(user)\n{status=ACTIVE, emailVerified=true}
activate UserService
UserService -> DB: UPDATE users SET status='ACTIVE',\nemailVerified=true WHERE id=?
DB --> UserService: Updated
deactivate UserService

AuthService --> Controller: EmailVerificationResponse\n{verified: true}
deactivate AuthService
Controller --> Customer: 200 OK\n"Email verified successfully"
deactivate Controller

@enduml
```

## 2. User Login with FCM Token

```plantuml
@startuml
title User Login with FCM Token Flow

actor Customer
participant "AuthController" as Controller
participant "AuthService" as AuthService
participant "UserService" as UserService
participant "JwtService" as JwtService
participant "FcmService" as FcmService
participant "TokenService" as TokenService
participant "Database" as DB

Customer -> Controller: POST /api/auth/login\n{email, password, fcmToken?}
activate Controller

Controller -> AuthService: login(LoginRequest)
activate AuthService

AuthService -> UserService: findByEmail(email)
activate UserService
UserService -> DB: SELECT * FROM users WHERE email=?
DB --> UserService: User found
deactivate UserService

AuthService -> AuthService: validatePassword(inputPassword, user.passwordHash)
alt Password invalid
    AuthService --> Controller: Exception: Invalid credentials
    Controller --> Customer: 401 Unauthorized
else Password valid
    AuthService -> AuthService: Check user.status == ACTIVE
    AuthService -> AuthService: Check user.emailVerified == true
    
    AuthService -> JwtService: generateAccessToken(user)
    activate JwtService
    JwtService --> AuthService: accessToken (expires 1h)
    deactivate JwtService
    
    AuthService -> JwtService: generateRefreshToken(user)
    activate JwtService
    JwtService --> AuthService: refreshToken (expires 7d)
    deactivate JwtService
    
    AuthService -> TokenService: saveRefreshToken(user, refreshToken)
    activate TokenService
    TokenService -> DB: INSERT INTO tokens\n(userId, token, type=REFRESH)
    deactivate TokenService
    
    alt FCM Token provided
        AuthService -> FcmService: updateFcmToken(userId, fcmToken)
        activate FcmService
        FcmService -> DB: UPDATE users SET fcm_token=?\nWHERE id=?
        deactivate FcmService
    end
    
    AuthService --> Controller: LoginResponse\n{accessToken, refreshToken, user}
    deactivate AuthService
    Controller --> Customer: 200 OK\n{tokens, userInfo}
end
deactivate Controller

@enduml
```

## 3. Forgot Password and Reset Password

```plantuml
@startuml
title Forgot Password and Reset Password Flow

actor Customer
participant "AuthController" as Controller
participant "AuthService" as AuthService
participant "UserService" as UserService
participant "OtpService" as OtpService
participant "EmailService" as EmailService
participant "Database" as DB

== Forgot Password ==
Customer -> Controller: POST /api/auth/forgot-password\n{email}
activate Controller

Controller -> AuthService: forgotPassword(ForgotPasswordRequest)
activate AuthService

AuthService -> UserService: findByEmail(email)
activate UserService
UserService -> DB: SELECT * FROM users WHERE email=?
DB --> UserService: User found
deactivate UserService

AuthService -> OtpService: generateOtp()
activate OtpService
OtpService --> AuthService: "654321"
deactivate OtpService

AuthService -> UserService: update(user)\n{passwordResetOtp, otpExpiry}
activate UserService
UserService -> DB: UPDATE users SET\npasswordResetOtp=?, otpExpiry=?\nWHERE id=?
DB --> UserService: Updated
deactivate UserService

AuthService -> EmailService: sendPasswordResetOtp(email, otp)
activate EmailService
EmailService -> EmailService: Load password-reset-otp.html
EmailService -> EmailService: Send email via SMTP
EmailService --> AuthService: Email sent
deactivate EmailService

AuthService --> Controller: Success
deactivate AuthService
Controller --> Customer: 200 OK\n"Password reset OTP sent"
deactivate Controller

== Reset Password ==
Customer -> Controller: POST /api/auth/reset-password\n{email, otp, newPassword}
activate Controller

Controller -> AuthService: resetPassword(ResetPasswordRequest)
activate AuthService

AuthService -> UserService: findByEmail(email)
activate UserService
UserService -> DB: SELECT * FROM users WHERE email=?
DB --> UserService: User with reset OTP
deactivate UserService

AuthService -> OtpService: validateOtp(user.passwordResetOtp, otp)
activate OtpService
OtpService -> OtpService: Check OTP match
OtpService -> OtpService: Check expiry time
alt OTP expired or invalid
    OtpService --> AuthService: Invalid
    AuthService --> Controller: Exception: Invalid or expired OTP
    Controller --> Customer: 400 Bad Request
else OTP valid
    OtpService --> AuthService: Valid
    deactivate OtpService
    
    AuthService -> AuthService: hashPassword(newPassword)
    
    AuthService -> UserService: update(user)\n{passwordHash, clearOtp}
    activate UserService
    UserService -> DB: UPDATE users SET\npasswordHash=?,\npasswordResetOtp=NULL\nWHERE id=?
    DB --> UserService: Updated
    deactivate UserService
    
    AuthService --> Controller: Success
    deactivate AuthService
    Controller --> Customer: 200 OK\n"Password reset successfully"
end
deactivate Controller

@enduml
```

## 4. Logout Flow

```plantuml
@startuml
title User Logout Flow

actor Customer
participant "AuthController" as Controller
participant "AuthService" as AuthService
participant "JwtService" as JwtService
participant "TokenBlacklistService" as BlacklistService
participant "FcmService" as FcmService
participant "Database" as DB

Customer -> Controller: POST /api/auth/logout\nHeader: Authorization: Bearer {token}
activate Controller

Controller -> AuthService: logout(bearerToken)
activate AuthService

AuthService -> JwtService: extractToken(bearerToken)
activate JwtService
JwtService --> AuthService: accessToken
deactivate JwtService

AuthService -> JwtService: getUserIdFromToken(accessToken)
activate JwtService
JwtService --> AuthService: userId
deactivate JwtService

AuthService -> BlacklistService: blacklistToken(accessToken)
activate BlacklistService
BlacklistService -> DB: INSERT INTO token_blacklist\n(token, expiresAt)
DB --> BlacklistService: Token blacklisted
deactivate BlacklistService

AuthService -> FcmService: removeFcmToken(userId)
activate FcmService
FcmService -> DB: UPDATE users SET fcm_token=NULL\nWHERE id=?
DB --> FcmService: FCM token removed
deactivate FcmService

AuthService --> Controller: Success
deactivate AuthService
Controller --> Customer: 200 OK\n"Logged out successfully"
deactivate Controller

@enduml
```

## 5. Refresh Token Flow

```plantuml
@startuml
title Refresh Token Flow

actor Customer
participant "AuthController" as Controller
participant "AuthService" as AuthService
participant "JwtService" as JwtService
participant "TokenService" as TokenService
participant "Database" as DB

Customer -> Controller: POST /api/auth/refresh\n{refreshToken}
activate Controller

Controller -> AuthService: refreshToken(RefreshTokenRequest)
activate AuthService

AuthService -> JwtService: validateRefreshToken(refreshToken)
activate JwtService
JwtService -> JwtService: Verify signature
JwtService -> JwtService: Check expiration
JwtService --> AuthService: Valid
deactivate JwtService

AuthService -> TokenService: isTokenValid(refreshToken)
activate TokenService
TokenService -> DB: SELECT * FROM tokens\nWHERE token=? AND type='REFRESH'
DB --> TokenService: Token found and valid
deactivate TokenService

AuthService -> JwtService: getUserIdFromToken(refreshToken)
activate JwtService
JwtService --> AuthService: userId
deactivate JwtService

AuthService -> JwtService: generateAccessToken(userId)
activate JwtService
JwtService --> AuthService: newAccessToken
deactivate JwtService

AuthService -> JwtService: generateRefreshToken(userId)
activate JwtService
JwtService --> AuthService: newRefreshToken
deactivate JwtService

AuthService -> TokenService: revokeOldToken(oldRefreshToken)
activate TokenService
TokenService -> DB: DELETE FROM tokens WHERE token=?
deactivate TokenService

AuthService -> TokenService: saveRefreshToken(userId, newRefreshToken)
activate TokenService
TokenService -> DB: INSERT INTO tokens
deactivate TokenService

AuthService --> Controller: LoginResponse\n{newAccessToken, newRefreshToken}
deactivate AuthService
Controller --> Customer: 200 OK\n{new tokens}
deactivate Controller

@enduml
```


