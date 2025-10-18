package com.officefood.healthy_food_api.controller;
import com.officefood.healthy_food_api.dto.request.OrderRequest;
import com.officefood.healthy_food_api.dto.response.OrderResponse;
import com.officefood.healthy_food_api.mapper.OrderMapper;
import com.officefood.healthy_food_api.model.Order;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.*; import java.util.stream.Collectors;

@RestController @RequestMapping("/api/orders") @RequiredArgsConstructor
public class OrderController {
    private final ServiceProvider sp;
    private final OrderMapper mapper;

    @GetMapping("/getall") public List<OrderResponse> list() {
        return sp.orders().findAll().stream().map(mapper::toResponse).collect(Collectors.toList());
    }
    @GetMapping("/getbyid") public ResponseEntity<OrderResponse> get(@PathVariable java.util.UUID id) {
        return sp.orders().findById(id).map(mapper::toResponse).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @PostMapping("/create") public OrderResponse create(@Valid @RequestBody OrderRequest req) {
        return mapper.toResponse(sp.orders().create(mapper.toEntity(req)));
    }
    @PutMapping("/update") public OrderResponse update(@PathVariable java.util.UUID id, @Valid @RequestBody OrderRequest req) {
        Order e = mapper.toEntity(req); e.setId(id); return mapper.toResponse(sp.orders().update(id, e));
    }
    @DeleteMapping("/delete") public ResponseEntity<Void> delete(@PathVariable java.util.UUID id) {
        sp.orders().deleteById(id); return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/recalc") public OrderResponse recalc(@PathVariable java.util.UUID id) { return mapper.toResponse(sp.orders().recalcTotals(id)); }
    @PostMapping("/{id}/apply-promo") public OrderResponse apply(@PathVariable java.util.UUID id, @RequestParam String code) { return mapper.toResponse(sp.orders().applyPromotion(id, code)); }
    @PostMapping("/{id}/confirm") public OrderResponse confirm(@PathVariable java.util.UUID id) { return mapper.toResponse(sp.orders().confirm(id)); }
    @PostMapping("/{id}/cancel") public OrderResponse cancel(@PathVariable java.util.UUID id, @RequestParam(required=false) String reason) { return mapper.toResponse(sp.orders().cancel(id, reason)); }
    @PostMapping("/{id}/complete") public OrderResponse complete(@PathVariable java.util.UUID id) { return mapper.toResponse(sp.orders().complete(id)); }

}
