package com.officefood.healthy_food_api.controller;
import com.officefood.healthy_food_api.dto.request.InventoryRequest;
import com.officefood.healthy_food_api.dto.response.InventoryResponse;
import com.officefood.healthy_food_api.mapper.InventoryMapper;
import com.officefood.healthy_food_api.model.Inventory;
import com.officefood.healthy_food_api.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.*; import java.util.stream.Collectors;

@RestController @RequestMapping("/api/inventorys") @RequiredArgsConstructor
public class InventoryController {
    private final InventoryService service;
    private final InventoryMapper mapper;

    @GetMapping("/getall") public List<InventoryResponse> list() {
        return service.findAll().stream().map(mapper::toResponse).collect(Collectors.toList());
    }
    @GetMapping("/getbyid") public ResponseEntity<InventoryResponse> get(@PathVariable java.util.UUID id) {
        return service.findById(id).map(mapper::toResponse).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @PostMapping("/create") public InventoryResponse create(@Valid @RequestBody InventoryRequest req) {
        return mapper.toResponse(service.create(mapper.toEntity(req)));
    }
    @PutMapping("/update") public InventoryResponse update(@PathVariable java.util.UUID id, @Valid @RequestBody InventoryRequest req) {
        Inventory e = mapper.toEntity(req); e.setId(id); return mapper.toResponse(service.update(id, e));
    }
    @DeleteMapping("/delete") public ResponseEntity<Void> delete(@PathVariable java.util.UUID id) {
        service.deleteById(id); return ResponseEntity.noContent().build();
    }

}
