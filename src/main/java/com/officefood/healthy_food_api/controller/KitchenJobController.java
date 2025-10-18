package com.officefood.healthy_food_api.controller;
import com.officefood.healthy_food_api.dto.request.KitchenJobRequest;
import com.officefood.healthy_food_api.dto.response.KitchenJobResponse;
import com.officefood.healthy_food_api.mapper.KitchenJobMapper;
import com.officefood.healthy_food_api.model.KitchenJob;
import com.officefood.healthy_food_api.service.KitchenJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.*; import java.util.stream.Collectors;

@RestController @RequestMapping("/api/kitchenJobs") @RequiredArgsConstructor
public class KitchenJobController {
    private final KitchenJobService service;
    private final KitchenJobMapper mapper;

    @GetMapping("/getall") public List<KitchenJobResponse> list() {
        return service.findAll().stream().map(mapper::toResponse).collect(Collectors.toList());
    }
    @GetMapping("/getbyid") public ResponseEntity<KitchenJobResponse> get(@PathVariable java.util.UUID id) {
        return service.findById(id).map(mapper::toResponse).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @PostMapping("/create") public KitchenJobResponse create(@Valid @RequestBody KitchenJobRequest req) {
        return mapper.toResponse(service.create(mapper.toEntity(req)));
    }
    @PutMapping("/update") public KitchenJobResponse update(@PathVariable java.util.UUID id, @Valid @RequestBody KitchenJobRequest req) {
        KitchenJob e = mapper.toEntity(req); e.setId(id); return mapper.toResponse(service.update(id, e));
    }
    @DeleteMapping("/delete") public ResponseEntity<Void> delete(@PathVariable java.util.UUID id) {
        service.deleteById(id); return ResponseEntity.noContent().build();
    }

}
