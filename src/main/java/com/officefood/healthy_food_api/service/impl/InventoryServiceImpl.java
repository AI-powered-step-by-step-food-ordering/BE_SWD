package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.model.Inventory;
import com.officefood.healthy_food_api.repository.InventoryRepository;
import com.officefood.healthy_food_api.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl extends CrudServiceImpl<Inventory> implements InventoryService {
    private final InventoryRepository repository;

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<Inventory, String> repo() {
        return repository;
    }

    @Override public void reserveForOrder(String orderId) { /* TODO */ }
    @Override public void consumeForOrder(String orderId) { /* TODO */ }
    @Override public void returnForCancelledOrder(String orderId) { /* TODO */ }

}
