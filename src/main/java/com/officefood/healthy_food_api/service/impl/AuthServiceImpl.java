package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.dto.LoginResponse;
import com.officefood.healthy_food_api.dto.EmailVerificationResponse;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final EmailService emailService;
    private final AuthMapper authMapper;

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest req) {
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
        
        // Generate email verification token
        String verificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationExpiry(OffsetDateTime.now().plusHours(24));
        user.setEmailVerified(false);
        
        userRepository.save(user);

        // Send verification email
        try {
            if (emailService != null) {
                log.info("🔄 Sending verification email to: {}", user.getEmail());
                boolean emailSent = emailService.sendVerificationEmail(
                    user.getFullName(),
                    user.getEmail(),
                    verificationToken
                );
                
                if (emailSent) {
                    log.info("✅ Verification email sent successfully to: {}", user.getEmail());
                } else {
                    log.error("❌ Failed to send verification email to: {}", user.getEmail());
                }
            } else {
                log.warn("⚠️ EmailService is null - verification email not sent");
            }
        } catch (Exception e) {
            log.error("❌ Exception while sending verification email: {}", e.getMessage(), e);
            // Don't fail registration if email sending fails
        }

        // Generate LoginResponse using mapper
        LoginResponse response = authMapper.toLoginResponse(user);
        
        // Set tokens manually since they're ignored in mapper
        String accessToken = jwtService.generateToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());
        
        return new LoginResponse(
            accessToken,
            refreshToken,
            "Bearer",
            user.getEmail(),
            user.getFullName(),
            jwtService.getAccessTokenExpiration()
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
            accessToken,
            refreshToken,
            "Bearer",
            user.getEmail(),
            user.getFullName(),
            jwtService.getAccessTokenExpiration()
        );
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
                newAccessToken,
                newRefreshToken,
                "Bearer",
                user.getEmail(),
                user.getFullName(),
                jwtService.getAccessTokenExpiration()
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
    public EmailVerificationResponse verifyEmail(String token) {
        Optional<User> userOpt = userRepository.findByEmailVerificationToken(token);
        
        if (userOpt.isEmpty()) {
            log.warn("Verification token not found: {}. User may have already verified their email.", token);
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        User user = userOpt.get();

        if (user.getEmailVerificationExpiry().isBefore(OffsetDateTime.now())) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        // Check if email already verified
        if (user.getEmailVerified()) {
            log.info("Email already verified for user: {}", user.getEmail());
            // Clear token if not already cleared
            if (user.getEmailVerificationToken() != null) {
                user.setEmailVerificationToken(null);
                user.setEmailVerificationExpiry(null);
                userRepository.save(user);
            }
            EmailVerificationResponse response = authMapper.toEmailVerificationResponse(user);
            return new EmailVerificationResponse(
                response.email(),
                response.fullName(),
                response.status(),
                response.emailVerified(),
                "Email already verified"
            );
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpiry(null);
        user.setStatus(AccountStatus.ACTIVE); // Change status to ACTIVE after verification
        userRepository.save(user);

        log.info("Email verified successfully for user: {} - Status changed to ACTIVE", user.getEmail());
        
        EmailVerificationResponse response = authMapper.toEmailVerificationResponse(user);
        return new EmailVerificationResponse(
            response.email(),
            response.fullName(),
            response.status(),
            response.emailVerified(),
            "Email verified successfully"
        );
    }

    @Override
    @Transactional
    public EmailVerificationResponse resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getEmailVerified()) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        // Generate new verification token
        String verificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationExpiry(OffsetDateTime.now().plusHours(24));
        userRepository.save(user);

        // Send verification email
        try {
            if (emailService != null) {
                boolean emailSent = emailService.sendVerificationEmail(
                    user.getFullName(),
                    user.getEmail(),
                    verificationToken
                );
                
                if (emailSent) {
                    log.info("✅ Verification email resent successfully to: {}", email);
                } else {
                    log.error("❌ Failed to resend verification email to: {}", email);
                }
            } else {
                log.warn("⚠️ EmailService is null - verification email not sent");
            }
        } catch (Exception e) {
            log.error("❌ Exception while resending verification email: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.EMAIL_SENDING_FAILED);
        }
        
        EmailVerificationResponse response = authMapper.toEmailVerificationResponse(user);
        return new EmailVerificationResponse(
            response.email(),
            response.fullName(),
            response.status(),
            response.emailVerified(),
            "Verification email sent successfully"
        );
    }
}