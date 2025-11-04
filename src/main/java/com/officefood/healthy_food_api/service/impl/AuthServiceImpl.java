package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.dto.LoginResponse;
import com.officefood.healthy_food_api.dto.EmailVerificationResponse;
import com.officefood.healthy_food_api.dto.VerifyOtpRequest;
import com.officefood.healthy_food_api.dto.ForgotPasswordRequest;
import com.officefood.healthy_food_api.dto.ResetPasswordRequest;
import com.officefood.healthy_food_api.dto.LoginRequest;
import com.officefood.healthy_food_api.dto.RegisterRequest;
import com.officefood.healthy_food_api.dto.RefreshTokenRequest;
import com.officefood.healthy_food_api.exception.AppException;
import com.officefood.healthy_food_api.exception.ErrorCode;
import com.officefood.healthy_food_api.mapper.AuthMapper;
import com.officefood.healthy_food_api.model.User;
import com.officefood.healthy_food_api.model.enums.AccountStatus;
import com.officefood.healthy_food_api.repository.UserRepository;
import com.officefood.healthy_food_api.service.AuthService;
import com.officefood.healthy_food_api.service.EmailService;
import com.officefood.healthy_food_api.service.JwtService;
import com.officefood.healthy_food_api.service.TokenBlacklistService;
import com.officefood.healthy_food_api.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

// imports cleaned after removing token-based verification

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final EmailService emailService;
    private final OtpService otpService;
    private final AuthMapper authMapper;

    @Value("${google.client-id}")
    private String googleClientId;

    @Override
    @Transactional
    public EmailVerificationResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // Validate password confirmation
        if (!req.getPassword().equals(req.getPasswordConfirm())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }

        // Create user entity using mapper
        User user = authMapper.toEntity(req);
        
        // Set password hash
        String hashedPassword = passwordEncoder.encode(req.getPassword());
        user.setPasswordHash(hashedPassword);
        
        // Generate and set OTP
        String verificationOtp = otpService.generateOtp();
        user.setEmailVerificationOtp(verificationOtp);
        user.setEmailVerificationOtpExpiry(OffsetDateTime.now().plusMinutes(10));
        user.setEmailVerified(false);
        user.setOtpAttempts(0);
        user.setStatus(AccountStatus.PENDING_VERIFICATION);
        userRepository.save(user);

        // Send verification OTP email
        try {
            if (emailService != null) {
                boolean emailSent = emailService.sendVerificationOtpEmail(
                        user.getFullName(), user.getEmail(), verificationOtp
                );
                if (!emailSent) {
                    log.error("Ã¢ÂÅ’ Failed to send verification OTP email to: {}", user.getEmail());
                } else {
                    log.info("Ã¢Å“â€¦ Verification OTP email sent successfully to: {}", user.getEmail());
                }
            }
        } catch (Exception e) {
            log.error("Ã¢ÂÅ’ Exception while sending verification OTP email: {}", e.getMessage(), e);
        }

        var response = authMapper.toEmailVerificationResponse(user);
        return new EmailVerificationResponse(
            response.email(), response.fullName(), response.status(), response.emailVerified(),
            "Email verification OTP sent"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
        
        // Check if email is verified
        if (!user.getEmailVerified()) {
            throw new AppException(ErrorCode.UNAUTHORIZED); // Email not verified
        }
        
        // Check if account is active (checks both status and isActive)
        if (!user.isAccountActive()) {
            throw new AppException(ErrorCode.UNAUTHORIZED); // Account not active
        }

        String accessToken = jwtService.generateToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        return new LoginResponse(
            user.getId(),
            accessToken,
            refreshToken,
            "Bearer",
            user.getEmail(),
            user.getFullName(),
            jwtService.getAccessTokenExpiration(),
            user.getGoalCode(),
            user.getRole() != null ? user.getRole().name() : null,
            user.getImageUrl(),
            user.getDateOfBirth(),
            user.getAddress(),
            user.getPhone()
        );
    }

    @Override
    @Transactional
    public LoginResponse loginWithGoogle(com.officefood.healthy_food_api.dto.GoogleLoginRequest req) {
        try {
            var transport = new com.google.api.client.http.javanet.NetHttpTransport();
            var jsonFactory = com.google.api.client.json.jackson2.JacksonFactory.getDefaultInstance();

            var verifier = new com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                    .setAudience(java.util.Collections.singletonList(googleClientId))
                    .build();

            com.google.api.client.googleapis.auth.oauth2.GoogleIdToken idToken = verifier.verify(req.idToken());
            if (idToken == null) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }

            com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            Boolean emailVerified = (Boolean) payload.get("email_verified");

            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                user = new User();
                user.setEmail(email);
                user.setFullName(name != null ? name : email);
                user.setImageUrl(pictureUrl);
                user.setEmailVerified(Boolean.TRUE.equals(emailVerified));
                user.setStatus(AccountStatus.ACTIVE);
                // Set a random password hash to satisfy non-null constraint
                String randomSecret = java.util.UUID.randomUUID().toString() + ":" + java.util.UUID.randomUUID();
                user.setPasswordHash(passwordEncoder.encode(randomSecret));
                userRepository.save(user);
            } else {
                // Ensure account is active
                if (!user.isAccountActive()) {
                    throw new AppException(ErrorCode.UNAUTHORIZED);
                }
                // Update avatar if empty and Google has one
                if ((user.getImageUrl() == null || user.getImageUrl().isBlank()) && pictureUrl != null) {
                    user.setImageUrl(pictureUrl);
                    userRepository.save(user);
                }
                // Trust Google email verification
                if (Boolean.TRUE.equals(emailVerified) && !Boolean.TRUE.equals(user.getEmailVerified())) {
                    user.setEmailVerified(true);
                    userRepository.save(user);
                }
            }

            String accessToken = jwtService.generateToken(user.getEmail());
            String refreshToken = jwtService.generateRefreshToken(user.getEmail());

            return new LoginResponse(
                user.getId(),
                accessToken,
                refreshToken,
                "Bearer",
                user.getEmail(),
                user.getFullName(),
                jwtService.getAccessTokenExpiration(),
                user.getGoalCode(),
                user.getRole() != null ? user.getRole().name() : null,
                user.getImageUrl(),
                user.getDateOfBirth(),
                user.getAddress(),
                user.getPhone()
            );
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google login failed: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse refreshToken(RefreshTokenRequest req) {
        try {
            String username = jwtService.extractUsername(req.refreshToken());

            if (!jwtService.isRefreshTokenValid(req.refreshToken(), username)) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }

            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            String newAccessToken = jwtService.generateToken(user.getEmail());
            String newRefreshToken = jwtService.generateRefreshToken(user.getEmail());

            return new LoginResponse(
                user.getId(),
                newAccessToken,
                newRefreshToken,
                "Bearer",
                user.getEmail(),
                user.getFullName(),
                jwtService.getAccessTokenExpiration(),
                user.getGoalCode(),
                user.getRole() != null ? user.getRole().name() : null,
                user.getImageUrl(),
                user.getDateOfBirth(),
                user.getAddress(),
                user.getPhone()
            );
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    @Override
    public void logout(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            tokenBlacklistService.blacklist(token);
        }
    }

    @Override
    @Transactional
    public EmailVerificationResponse verifyOtp(VerifyOtpRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getEmailVerified()) {
            EmailVerificationResponse response = authMapper.toEmailVerificationResponse(user);
            return new EmailVerificationResponse(
                response.email(), response.fullName(), response.status(), response.emailVerified(),
                "Email already verified"
            );
        }

        if (user.getEmailVerificationOtp() == null ||
            user.getEmailVerificationOtpExpiry() == null ||
            user.getEmailVerificationOtpExpiry().isBefore(OffsetDateTime.now())) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        if (user.getOtpAttempts() != null && user.getOtpAttempts() >= 5) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (!otpService.validateOtp(request.otp(), user.getEmailVerificationOtp())) {
            user.setOtpAttempts((user.getOtpAttempts() == null ? 0 : user.getOtpAttempts()) + 1);
            userRepository.save(user);
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        user.setEmailVerified(true);
        user.setEmailVerificationOtp(null);
        user.setEmailVerificationOtpExpiry(null);
        user.setOtpAttempts(0);
        user.setStatus(AccountStatus.ACTIVE);
        userRepository.save(user);

        EmailVerificationResponse response = authMapper.toEmailVerificationResponse(user);
        return new EmailVerificationResponse(
            response.email(), response.fullName(), response.status(), response.emailVerified(),
            "Email verified successfully"
        );
    }

    @Override
    @Transactional
    public EmailVerificationResponse resendVerificationOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getEmailVerified()) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        String verificationOtp = otpService.generateOtp();
        user.setEmailVerificationOtp(verificationOtp);
        user.setEmailVerificationOtpExpiry(OffsetDateTime.now().plusMinutes(10));
        user.setOtpAttempts(0);
        userRepository.save(user);

        try {
            if (emailService != null) {
                boolean emailSent = emailService.sendVerificationOtpEmail(
                        user.getFullName(), user.getEmail(), verificationOtp
                );
                if (!emailSent) {
                    throw new AppException(ErrorCode.EMAIL_SENDING_FAILED);
                }
            }
        } catch (Exception e) {
            throw new AppException(ErrorCode.EMAIL_SENDING_FAILED);
        }

        EmailVerificationResponse response = authMapper.toEmailVerificationResponse(user);
        return new EmailVerificationResponse(
                response.email(), response.fullName(), response.status(), response.emailVerified(),
                "Verification OTP sent successfully"
        );
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String otp = otpService.generateOtp();
        user.setPasswordResetOtp(otp);
        user.setPasswordResetOtpExpiry(OffsetDateTime.now().plusMinutes(10));
        user.setPasswordResetAttempts(0);
        userRepository.save(user);

        boolean sent = emailService.sendPasswordResetOtpEmail(user.getFullName(), user.getEmail(), otp);
        if (!sent) {
            throw new AppException(ErrorCode.EMAIL_SENDING_FAILED);
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.newPassword().equals(request.passwordConfirm())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getPasswordResetOtp() == null ||
            user.getPasswordResetOtpExpiry() == null ||
            user.getPasswordResetOtpExpiry().isBefore(OffsetDateTime.now())) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        if (user.getPasswordResetAttempts() != null && user.getPasswordResetAttempts() >= 5) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (!otpService.validateOtp(request.otp(), user.getPasswordResetOtp())) {
            user.setPasswordResetAttempts((user.getPasswordResetAttempts() == null ? 0 : user.getPasswordResetAttempts()) + 1);
            userRepository.save(user);
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setPasswordResetOtp(null);
        user.setPasswordResetOtpExpiry(null);
        user.setPasswordResetAttempts(0);
        userRepository.save(user);
    }
}