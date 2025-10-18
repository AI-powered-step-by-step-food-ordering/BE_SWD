package com.officefood.healthy_food_api.controller;
import com.officefood.healthy_food_api.dto.request.TokenRequest;
import com.officefood.healthy_food_api.dto.response.TokenResponse;
import com.officefood.healthy_food_api.mapper.TokenMapper;
import com.officefood.healthy_food_api.model.Token;
import com.officefood.healthy_food_api.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.*; import java.util.stream.Collectors;

@RestController @RequestMapping("/api/tokens") @RequiredArgsConstructor
public class TokenController {
    private final TokenService service;
    private final TokenMapper mapper;

    @GetMapping("/getall")public List<TokenResponse> list() {
        return service.findAll().stream().map(mapper::toResponse).collect(Collectors.toList());
    }
    @GetMapping("/getbyid") public ResponseEntity<TokenResponse> get(@PathVariable java.util.UUID id) {
        return service.findById(id).map(mapper::toResponse).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @PostMapping("/create") public TokenResponse create(@Valid @RequestBody TokenRequest req) {
        return mapper.toResponse(service.create(mapper.toEntity(req)));
    }
    @PutMapping("/update") public TokenResponse update(@PathVariable java.util.UUID id, @Valid @RequestBody TokenRequest req) {
        Token e = mapper.toEntity(req); e.setId(id); return mapper.toResponse(service.update(id, e));
    }
    @DeleteMapping("/delete") public ResponseEntity<Void> delete(@PathVariable java.util.UUID id) {
        service.deleteById(id); return ResponseEntity.noContent().build();
    }

}
