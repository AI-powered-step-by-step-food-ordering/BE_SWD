package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.config.JwtUtil;
import com.officefood.healthy_food_api.dto.AuthResponse;
import com.officefood.healthy_food_api.dto.LoginRequest;
import com.officefood.healthy_food_api.dto.RegisterRequest;
import com.officefood.healthy_food_api.model.User;
import com.officefood.healthy_food_api.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalStateException("Email already registered");
        }

        User user = new User();
        // Nếu DB là CHAR(36) và bạn KHÔNG dùng GenerationType.UUID:
         user.setId(UUID.randomUUID());

        user.setFullName(req.getFullName());
        user.setEmail(req.getEmail());
        user.setCompanyName(req.getCompanyName());
        user.setGoalCode(req.getGoalCode());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, "Bearer", user.getEmail(), user.getFullName());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, "Bearer", user.getEmail(), user.getFullName());
    }
}
