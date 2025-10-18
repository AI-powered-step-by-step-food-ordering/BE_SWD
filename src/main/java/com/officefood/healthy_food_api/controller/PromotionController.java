package com.officefood.healthy_food_api.controller;
import com.officefood.healthy_food_api.dto.request.PromotionRequest;
import com.officefood.healthy_food_api.dto.response.PromotionResponse;
import com.officefood.healthy_food_api.mapper.PromotionMapper;
import com.officefood.healthy_food_api.model.Promotion;
import com.officefood.healthy_food_api.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.*; import java.util.stream.Collectors;

@RestController @RequestMapping("/api/promotions") @RequiredArgsConstructor
public class PromotionController {
    private final PromotionService service;
    private final PromotionMapper mapper;

    @GetMapping("/getall") public List<PromotionResponse> list() {
        return service.findAll().stream().map(mapper::toResponse).collect(Collectors.toList());
    }
    @GetMapping("/getbyid") public ResponseEntity<PromotionResponse> get(@PathVariable java.util.UUID id) {
        return service.findById(id).map(mapper::toResponse).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @PostMapping("/create") public PromotionResponse create(@Valid @RequestBody PromotionRequest req) {
        return mapper.toResponse(service.create(mapper.toEntity(req)));
    }
    @PutMapping("/update") public PromotionResponse update(@PathVariable java.util.UUID id, @Valid @RequestBody PromotionRequest req) {
        Promotion e = mapper.toEntity(req); e.setId(id); return mapper.toResponse(service.update(id, e));
    }
    @DeleteMapping("/delete") public ResponseEntity<Void> delete(@PathVariable java.util.UUID id) {
        service.deleteById(id); return ResponseEntity.noContent().build();
    }

}
