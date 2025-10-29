package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.controller.base.BaseController;
import com.officefood.healthy_food_api.dto.request.UserRequest;
import com.officefood.healthy_food_api.dto.request.UserUpdateRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.UserResponse;
import com.officefood.healthy_food_api.mapper.UserMapper;
import com.officefood.healthy_food_api.model.User;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import com.officefood.healthy_food_api.service.CrudService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController extends BaseController<User, UserRequest, UserResponse> {
    private final ServiceProvider sp;
    private final UserMapper mapper;

    @Override
    protected CrudService<User> getService() {
        return sp.users();
    }

    @Override
    protected UserResponse toResponse(User entity) {
        return mapper.toResponse(entity);
    }

    @Override
    protected User toEntity(UserRequest request) {
        return mapper.toEntity(request);
    }

    // POST /api/users/create
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody UserRequest req) {
        UserResponse response = mapper.toResponse(sp.users().create(mapper.toEntity(req)));
        return ResponseEntity.ok(ApiResponse.success(201, "User created successfully", response));
    }

    // PUT /api/users/update/{id}
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> update(@PathVariable UUID id,
                              @Valid @RequestBody UserUpdateRequest req) {
        try {
            // Get existing user
            User existingUser = sp.users().findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Update only allowed fields
            existingUser.setFullName(req.getFullName());
            existingUser.setEmail(req.getEmail());
            existingUser.setGoalCode(req.getGoalCode());

            // Update status if provided - with validation
            if (req.getStatus() != null && !req.getStatus().trim().isEmpty()) {
                try {
                    existingUser.setStatus(com.officefood.healthy_food_api.model.enums.AccountStatus.valueOf(req.getStatus().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.ok(ApiResponse.error(400, "INVALID_STATUS",
                        "Invalid status. Must be one of: ACTIVE, SUSPENDED, DELETED, PENDING_VERIFICATION"));
                }
            }

            // Update imageUrl if provided
            if (req.getImageUrl() != null) {
                existingUser.setImageUrl(req.getImageUrl());
            }

            // Password and Role are NOT updated (preserved automatically)

            UserResponse response = mapper.toResponse(sp.users().update(id, existingUser));
            return ResponseEntity.ok(ApiResponse.success(200, "User updated successfully", response));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(500, "UPDATE_FAILED",
                "Failed to update user: " + e.getMessage()));
        }
    }
}
