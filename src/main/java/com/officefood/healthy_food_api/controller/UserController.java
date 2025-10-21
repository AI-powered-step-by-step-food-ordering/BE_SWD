package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.dto.request.UserRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.UserResponse;
import com.officefood.healthy_food_api.mapper.UserMapper;
import com.officefood.healthy_food_api.model.User;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final ServiceProvider sp;
    private final UserMapper mapper;

    // GET /api/users/getall
    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAll() {
        List<UserResponse> users = sp.users()
                 .findAll()
                 .stream()
                 .map(mapper::toResponse)
                 .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(200, "Users retrieved successfully", users));
    }

    // GET /api/users/getbyid/{id}
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable UUID id) {
        return sp.users()
                 .findById(id)
                 .map(mapper::toResponse)
                 .map(user -> ResponseEntity.ok(ApiResponse.success(200, "User retrieved successfully", user)))
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
    public ResponseEntity<ApiResponse<UserResponse>> update(@PathVariable UUID id,
                              @Valid @RequestBody UserRequest req) {
        User entity = mapper.toEntity(req);
        entity.setId(id);
        UserResponse response = mapper.toResponse(sp.users().update(id, entity));
        return ResponseEntity.ok(ApiResponse.success(200, "User updated successfully", response));
    }

    // DELETE /api/users/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        sp.users().deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "User deleted successfully", null));
    }
}
