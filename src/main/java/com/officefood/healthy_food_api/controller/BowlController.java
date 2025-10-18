package com.officefood.healthy_food_api.controller;
import com.officefood.healthy_food_api.dto.request.BowlRequest;
import com.officefood.healthy_food_api.dto.response.BowlResponse;
import com.officefood.healthy_food_api.mapper.BowlMapper;
import com.officefood.healthy_food_api.model.Bowl;
import com.officefood.healthy_food_api.service.BowlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.*; import java.util.stream.Collectors;

@RestController @RequestMapping("/api/bowls") @RequiredArgsConstructor
public class BowlController {
    private final BowlService service;
    private final BowlMapper mapper;

    @GetMapping("/getall") public List<BowlResponse> list() {
        return service.findAll().stream().map(mapper::toResponse).collect(Collectors.toList());
    }
    @GetMapping("/getbyid") public ResponseEntity<BowlResponse> get(@PathVariable java.util.UUID id) {
        return service.findById(id).map(mapper::toResponse).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @PostMapping("/create") public BowlResponse create(@Valid @RequestBody BowlRequest req) {
        return mapper.toResponse(service.create(mapper.toEntity(req)));
    }
    @PutMapping("/update") public BowlResponse update(@PathVariable java.util.UUID id, @Valid @RequestBody BowlRequest req) {
        Bowl e = mapper.toEntity(req); e.setId(id); return mapper.toResponse(service.update(id, e));
    }
    @DeleteMapping("/delete") public ResponseEntity<Void> delete(@PathVariable java.util.UUID id) {
        service.deleteById(id); return ResponseEntity.noContent().build();
    }

}
