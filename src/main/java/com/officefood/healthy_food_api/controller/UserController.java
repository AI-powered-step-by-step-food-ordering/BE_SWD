package com.officefood.healthy_food_api.controller;
import com.officefood.healthy_food_api.dto.request.UserRequest;
import com.officefood.healthy_food_api.dto.response.UserResponse;
import com.officefood.healthy_food_api.mapper.UserMapper;
import com.officefood.healthy_food_api.model.User;
import com.officefood.healthy_food_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.*; import java.util.stream.Collectors;

@RestController @RequestMapping("/api/users") @RequiredArgsConstructor
public class UserController {
    private final UserService service;
    private final UserMapper mapper;

    @GetMapping("/getall") public List<UserResponse> list() {
        return service.findAll().stream().map(mapper::toResponse).collect(Collectors.toList());
    }
    @GetMapping("/getbyid") public ResponseEntity<UserResponse> get(@PathVariable java.util.UUID id) {
        return service.findById(id).map(mapper::toResponse).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @PostMapping("/create") public UserResponse create(@Valid @RequestBody UserRequest req) {
        return mapper.toResponse(service.create(mapper.toEntity(req)));
    }
    @PutMapping("/update") public UserResponse update(@PathVariable java.util.UUID id, @Valid @RequestBody UserRequest req) {
        User e = mapper.toEntity(req); e.setId(id); return mapper.toResponse(service.update(id, e));
    }
    @DeleteMapping("/delete") public ResponseEntity<Void> delete(@PathVariable java.util.UUID id) {
        service.deleteById(id); return ResponseEntity.noContent().build();
    }

}
