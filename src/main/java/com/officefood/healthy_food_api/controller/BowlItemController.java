package com.officefood.healthy_food_api.controller;
import com.officefood.healthy_food_api.dto.request.BowlItemRequest;
import com.officefood.healthy_food_api.dto.response.BowlItemResponse;
import com.officefood.healthy_food_api.mapper.BowlItemMapper;
import com.officefood.healthy_food_api.model.BowlItem;
import com.officefood.healthy_food_api.service.BowlItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.*; import java.util.stream.Collectors;

@RestController @RequestMapping("/api/bowlItems") @RequiredArgsConstructor
public class BowlItemController {
    private final BowlItemService service;
    private final BowlItemMapper mapper;

    @GetMapping("/getall") public List<BowlItemResponse> list() {
        return service.findAll().stream().map(mapper::toResponse).collect(Collectors.toList());
    }
    @GetMapping("/getbyid") public ResponseEntity<BowlItemResponse> get(@PathVariable java.util.UUID id) {
        return service.findById(id).map(mapper::toResponse).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @PostMapping("/create") public BowlItemResponse create(@Valid @RequestBody BowlItemRequest req) {
        return mapper.toResponse(service.create(mapper.toEntity(req)));
    }
    @PutMapping("/update") public BowlItemResponse update(@PathVariable java.util.UUID id, @Valid @RequestBody BowlItemRequest req) {
        BowlItem e = mapper.toEntity(req); e.setId(id); return mapper.toResponse(service.update(id, e));
    }
    @DeleteMapping("/delete") public ResponseEntity<Void> delete(@PathVariable java.util.UUID id) {
        service.deleteById(id); return ResponseEntity.noContent().build();
    }

}
