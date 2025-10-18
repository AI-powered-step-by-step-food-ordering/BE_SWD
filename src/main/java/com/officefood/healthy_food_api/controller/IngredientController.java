package com.officefood.healthy_food_api.controller;
import com.officefood.healthy_food_api.dto.request.IngredientRequest;
import com.officefood.healthy_food_api.dto.response.IngredientResponse;
import com.officefood.healthy_food_api.mapper.IngredientMapper;
import com.officefood.healthy_food_api.model.Ingredient;
import com.officefood.healthy_food_api.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.*; import java.util.stream.Collectors;

@RestController @RequestMapping("/api/ingredients") @RequiredArgsConstructor
public class IngredientController {
    private final IngredientService service;
    private final IngredientMapper mapper;

    @GetMapping("/getall") public List<IngredientResponse> list() {
        return service.findAll().stream().map(mapper::toResponse).collect(Collectors.toList());
    }
    @GetMapping("/getbyid") public ResponseEntity<IngredientResponse> get(@PathVariable java.util.UUID id) {
        return service.findById(id).map(mapper::toResponse).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @PostMapping("/create") public IngredientResponse create(@Valid @RequestBody IngredientRequest req) {
        return mapper.toResponse(service.create(mapper.toEntity(req)));
    }
    @PutMapping("/update") public IngredientResponse update(@PathVariable java.util.UUID id, @Valid @RequestBody IngredientRequest req) {
        Ingredient e = mapper.toEntity(req); e.setId(id); return mapper.toResponse(service.update(id, e));
    }
    @DeleteMapping("/delete") public ResponseEntity<Void> delete(@PathVariable java.util.UUID id) {
        service.deleteById(id); return ResponseEntity.noContent().build();
    }

}
