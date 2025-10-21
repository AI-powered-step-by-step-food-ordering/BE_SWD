package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.model.Inventory;
import com.officefood.healthy_food_api.repository.InventoryRepository;
import com.officefood.healthy_food_api.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl extends CrudServiceImpl<Inventory> implements InventoryService {
    private final InventoryRepository repository;

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<Inventory, UUID> repo() {
        return repository;
    }

    @Override public void reserveForOrder(UUID orderId) { /* TODO */ }
    @Override public void consumeForOrder(UUID orderId) { /* TODO */ }
    @Override public void returnForCancelledOrder(UUID orderId) { /* TODO */ }

}
