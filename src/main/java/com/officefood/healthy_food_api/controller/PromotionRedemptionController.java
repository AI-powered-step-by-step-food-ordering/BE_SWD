package com.officefood.healthy_food_api.controller;
import com.officefood.healthy_food_api.dto.request.PromotionRedemptionRequest;
import com.officefood.healthy_food_api.dto.response.PromotionRedemptionResponse;
import com.officefood.healthy_food_api.mapper.PromotionRedemptionMapper;
import com.officefood.healthy_food_api.model.PromotionRedemption;
import com.officefood.healthy_food_api.service.PromotionRedemptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.*; import java.util.stream.Collectors;

@RestController @RequestMapping("/api/promotionRedemptions") @RequiredArgsConstructor
public class PromotionRedemptionController {
    private final PromotionRedemptionService service;
    private final PromotionRedemptionMapper mapper;

    @GetMapping("/getall") public List<PromotionRedemptionResponse> list() {
        return service.findAll().stream().map(mapper::toResponse).collect(Collectors.toList());
    }
    @GetMapping("/getbyid") public ResponseEntity<PromotionRedemptionResponse> get(@PathVariable java.util.UUID id) {
        return service.findById(id).map(mapper::toResponse).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @PostMapping("/create") public PromotionRedemptionResponse create(@Valid @RequestBody PromotionRedemptionRequest req) {
        return mapper.toResponse(service.create(mapper.toEntity(req)));
    }
    @PutMapping("/update") public PromotionRedemptionResponse update(@PathVariable java.util.UUID id, @Valid @RequestBody PromotionRedemptionRequest req) {
        PromotionRedemption e = mapper.toEntity(req); e.setId(id); return mapper.toResponse(service.update(id, e));
    }
    @DeleteMapping("/delete") public ResponseEntity<Void> delete(@PathVariable java.util.UUID id) {
        service.deleteById(id); return ResponseEntity.noContent().build();
    }

}
