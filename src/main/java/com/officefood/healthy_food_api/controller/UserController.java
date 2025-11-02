package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.controller.base.BaseController;
import com.officefood.healthy_food_api.dto.request.UserRequest;
import com.officefood.healthy_food_api.dto.request.UserUpdateRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.PagedResponse;
import com.officefood.healthy_food_api.dto.response.UserResponse;
import com.officefood.healthy_food_api.mapper.UserMapper;
import com.officefood.healthy_food_api.model.User;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import com.officefood.healthy_food_api.service.CrudService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;


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

    /**
     * Override getAllActive() from BaseController
     * User entity checks status = ACTIVE instead of isActive
     * GET /api/users/active
     */
    @Override
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllActive(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        java.util.List<User> activeUsers = sp.users()
                .findAll()
                .stream()
                .filter(User::isAccountActive) // Check status = ACTIVE and deletedAt = null
                .collect(java.util.stream.Collectors.toList());
        java.util.List<User> sortedUsers = sortEntities(activeUsers, sortBy, sortDir);
        PagedResponse<UserResponse> pagedResponse = createPagedResponse(sortedUsers, page, size);
        return ResponseEntity.ok(ApiResponse.success(200, "Retrieved active users successfully", pagedResponse));
    }

    /**
     * Override getAllInactive() from BaseController
     * User entity checks status != ACTIVE instead of isActive
     * GET /api/users/inactive
     */
    @Override
    @GetMapping("/inactive")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllInactive(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        java.util.List<User> inactiveUsers = sp.users()
                .findAll()
                .stream()
                .filter(user -> !user.isAccountActive()) // Check status != ACTIVE or deletedAt != null
                .collect(java.util.stream.Collectors.toList());
        java.util.List<User> sortedUsers = sortEntities(inactiveUsers, sortBy, sortDir);
        PagedResponse<UserResponse> pagedResponse = createPagedResponse(sortedUsers, page, size);
        return ResponseEntity.ok(ApiResponse.success(200, "Retrieved inactive users successfully", pagedResponse));
    }

    /**
     * Override getById() from BaseController
     * User entity checks isAccountActive() instead of isActive
     * For regular users - only returns ACTIVE users, no createdAt
     * GET /api/users/getbyid/{id}
     */
    @Override
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable String id) {
        return sp.users()
                .findById(id)
                .filter(User::isAccountActive) // Only ACTIVE users
                .map(mapper::toResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(200, "User retrieved successfully", response)))
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "User not found or inactive")));
    }

    /**
     * Get user by ID for admin - includes createdAt and returns both ACTIVE and DELETED users
     * GET /api/users/admin/getbyid/{id}
     */
    @GetMapping("/admin/getbyid/{id}")
    public ResponseEntity<ApiResponse<com.officefood.healthy_food_api.dto.response.UserAdminResponse>> getByIdForAdmin(@PathVariable String id) {
        return sp.users()
                .findById(id)
                .map(mapper::toAdminResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(200, "User retrieved successfully", response)))
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "User not found")));
    }

    // POST /api/users/create
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody UserRequest req) {
        UserResponse response = mapper.toResponse(sp.users().create(mapper.toEntity(req)));
        return ResponseEntity.ok(ApiResponse.success(201, "User created successfully", response));
    }

    // PUT /api/users/update/{id}
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> update(@PathVariable String id,
                              @Valid @RequestBody UserUpdateRequest req) {
        try {
            // Get existing user
            User existingUser = sp.users().findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Update only allowed fields
            existingUser.setFullName(req.getFullName());
            existingUser.setEmail(req.getEmail());
            existingUser.setGoalCode(req.getGoalCode());

            // Update optional fields if provided
            if (req.getImageUrl() != null) {
                existingUser.setImageUrl(req.getImageUrl());
            }
            if (req.getDateOfBirth() != null) {
                existingUser.setDateOfBirth(req.getDateOfBirth());
            }
            if (req.getAddress() != null) {
                existingUser.setAddress(req.getAddress());
            }
            if (req.getPhone() != null) {
                existingUser.setPhone(req.getPhone());
            }

            // Password, Role, and Status are NOT updated here
            // Use /soft-delete/{id} to delete (set status = DELETED)
            // Use /restore/{id} to restore (set status = ACTIVE)

            UserResponse response = mapper.toResponse(sp.users().update(id, existingUser));
            return ResponseEntity.ok(ApiResponse.success(200, "User updated successfully", response));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(500, "UPDATE_FAILED",
                "Failed to update user: " + e.getMessage()));
        }
    }


    /**
     * Override restore() from BaseController
     * User entity uses 'status' enum instead of 'isActive' boolean
     * PUT /api/users/restore/{id}
     */
    @Override
    @PutMapping("/restore/{id}")
    public ResponseEntity<ApiResponse<Void>> restore(@PathVariable String id) {
        return sp.users().findById(id)
                .map(user -> {
                    // User-specific restore: set status = ACTIVE and clear deletedAt
                    user.restore(); // This sets status=ACTIVE, deletedAt=null
                    sp.users().update(id, user);
                    return ResponseEntity.ok(ApiResponse.<Void>success(200, "User restored successfully", null));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "User not found")));
    }

    /**
     * Override softDelete() from BaseController
     * User entity uses 'status' enum instead of 'isActive' boolean
     * PUT /api/users/soft-delete/{id}
     */
    @Override
    @PutMapping("/soft-delete/{id}")
    public ResponseEntity<ApiResponse<Void>> softDelete(@PathVariable String id) {
        return sp.users().findById(id)
                .map(user -> {
                    // User-specific soft delete: set status = DELETED and set deletedAt
                    user.softDelete(); // This sets status=DELETED, deletedAt=now
                    sp.users().update(id, user);
                    return ResponseEntity.ok(ApiResponse.<Void>success(200, "User soft deleted successfully", null));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "User not found")));
    }

    // DELETE /api/users/delete/{id} - inherited from BaseController
}

