package com.officefood.healthy_food_api.controller;
import com.officefood.healthy_food_api.dto.request.BowlTemplateRequest;
import com.officefood.healthy_food_api.dto.response.BowlTemplateResponse;
import com.officefood.healthy_food_api.mapper.BowlTemplateMapper;
import com.officefood.healthy_food_api.model.BowlTemplate;
import com.officefood.healthy_food_api.service.BowlTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.*; import java.util.stream.Collectors;

@RestController @RequestMapping("/api/bowlTemplates") @RequiredArgsConstructor
public class BowlTemplateController {
    private final BowlTemplateService service;
    private final BowlTemplateMapper mapper;

    @GetMapping("/getall") public List<BowlTemplateResponse> list() {
        return service.findAll().stream().map(mapper::toResponse).collect(Collectors.toList());
    }
    @GetMapping("/getbyid") public ResponseEntity<BowlTemplateResponse> get(@PathVariable java.util.UUID id) {
        return service.findById(id).map(mapper::toResponse).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @PostMapping("/create") public BowlTemplateResponse create(@Valid @RequestBody BowlTemplateRequest req) {
        return mapper.toResponse(service.create(mapper.toEntity(req)));
    }
    @PutMapping("/update") public BowlTemplateResponse update(@PathVariable java.util.UUID id, @Valid @RequestBody BowlTemplateRequest req) {
        BowlTemplate e = mapper.toEntity(req); e.setId(id); return mapper.toResponse(service.update(id, e));
    }
    @DeleteMapping("/delete") public ResponseEntity<Void> delete(@PathVariable java.util.UUID id) {
        service.deleteById(id); return ResponseEntity.noContent().build();
    }

}
