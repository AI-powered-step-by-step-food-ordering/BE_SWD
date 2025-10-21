package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.dto.AuthResponse;
import com.officefood.healthy_food_api.dto.LoginRequest;
import com.officefood.healthy_food_api.dto.RegisterRequest;
import com.officefood.healthy_food_api.dto.RefreshTokenRequest;
import com.officefood.healthy_food_api.exception.AppException;
import com.officefood.healthy_food_api.exception.ErrorCode;
import com.officefood.healthy_food_api.model.User;
import com.officefood.healthy_food_api.repository.UserRepository;
import com.officefood.healthy_food_api.service.AuthService;
import com.officefood.healthy_food_api.service.JwtService;
import com.officefood.healthy_food_api.service.TokenBlacklistService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           TokenBlacklistService tokenBlacklistService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS, "Email already registered");
        }

        // Create new user
        User user = new User();
        user.setFullName(req.getFullName());
        user.setEmail(req.getEmail());
        user.setGoalCode(req.getGoalCode());

        // Set password hash
        String hashedPassword = passwordEncoder.encode(req.getPassword());
        user.setPasswordHash(hashedPassword);

        // Set default role if not provided
        user.setRole(req.getRole() != null ? req.getRole() : com.officefood.healthy_food_api.model.enums.Role.USER);
        user.setCreatedAt(OffsetDateTime.now());
        userRepository.save(user);

        String accessToken = jwtService.generateToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        return new AuthResponse(
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
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS, "Invalid credentials");
        }

        String accessToken = jwtService.generateToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        return new AuthResponse(
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
    public AuthResponse refreshToken(RefreshTokenRequest req) {
        try {
            String username = jwtService.extractUsername(req.refreshToken());

            if (!jwtService.isRefreshTokenValid(req.refreshToken(), username)) {
                throw new AppException(ErrorCode.UNAUTHORIZED, "Invalid refresh token");
            }

            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found"));

            String newAccessToken = jwtService.generateToken(user.getEmail());
            String newRefreshToken = jwtService.generateRefreshToken(user.getEmail());

            return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                user.getEmail(),
                user.getFullName(),
                jwtService.getAccessTokenExpiration()
            );
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Invalid refresh token");
        }
    }

    @Override
    public void logout(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            tokenBlacklistService.blacklist(token);
        }
    }
}
