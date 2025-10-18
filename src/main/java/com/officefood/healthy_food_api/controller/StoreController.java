package com.officefood.healthy_food_api.controller;
import com.officefood.healthy_food_api.dto.request.StoreRequest;
import com.officefood.healthy_food_api.dto.response.StoreResponse;
import com.officefood.healthy_food_api.mapper.StoreMapper;
import com.officefood.healthy_food_api.model.Store;
import com.officefood.healthy_food_api.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.*; import java.util.stream.Collectors;

@RestController @RequestMapping("/api/stores") @RequiredArgsConstructor
public class StoreController {
    private final StoreService service;
    private final StoreMapper mapper;

    @GetMapping("/getall") public List<StoreResponse> list() {
        return service.findAll().stream().map(mapper::toResponse).collect(Collectors.toList());
    }
    @GetMapping("/getbyid") public ResponseEntity<StoreResponse> get(@PathVariable java.util.UUID id) {
        return service.findById(id).map(mapper::toResponse).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @PostMapping("/create") public StoreResponse create(@Valid @RequestBody StoreRequest req) {
        return mapper.toResponse(service.create(mapper.toEntity(req)));
    }
    @PutMapping("/update") public StoreResponse update(@PathVariable java.util.UUID id, @Valid @RequestBody StoreRequest req) {
        Store e = mapper.toEntity(req); e.setId(id); return mapper.toResponse(service.update(id, e));
    }
    @DeleteMapping("/delete") public ResponseEntity<Void> delete(@PathVariable java.util.UUID id) {
        service.deleteById(id); return ResponseEntity.noContent().build();
    }

}
