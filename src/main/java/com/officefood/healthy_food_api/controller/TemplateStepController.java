package com.officefood.healthy_food_api.controller;
import com.officefood.healthy_food_api.dto.request.TemplateStepRequest;
import com.officefood.healthy_food_api.dto.response.TemplateStepResponse;
import com.officefood.healthy_food_api.mapper.TemplateStepMapper;
import com.officefood.healthy_food_api.model.TemplateStep;
import com.officefood.healthy_food_api.service.TemplateStepService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.*; import java.util.stream.Collectors;

@RestController @RequestMapping("/api/templateSteps") @RequiredArgsConstructor
public class TemplateStepController {
    private final TemplateStepService service;
    private final TemplateStepMapper mapper;

    @GetMapping("/getall") public List<TemplateStepResponse> list() {
        return service.findAll().stream().map(mapper::toResponse).collect(Collectors.toList());
    }
    @GetMapping("/getbyid") public ResponseEntity<TemplateStepResponse> get(@PathVariable java.util.UUID id) {
        return service.findById(id).map(mapper::toResponse).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @PostMapping("/create") public TemplateStepResponse create(@Valid @RequestBody TemplateStepRequest req) {
        return mapper.toResponse(service.create(mapper.toEntity(req)));
    }
    @PutMapping("/update") public TemplateStepResponse update(@PathVariable java.util.UUID id, @Valid @RequestBody TemplateStepRequest req) {
        TemplateStep e = mapper.toEntity(req); e.setId(id); return mapper.toResponse(service.update(id, e));
    }
    @DeleteMapping("/delete") public ResponseEntity<Void> delete(@PathVariable java.util.UUID id) {
        service.deleteById(id); return ResponseEntity.noContent().build();
    }

}
