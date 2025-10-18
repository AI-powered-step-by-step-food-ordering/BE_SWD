package com.officefood.healthy_food_api.service.impl;
import com.officefood.healthy_food_api.model.*;
import com.officefood.healthy_food_api.model.enums.*;
import com.officefood.healthy_food_api.repository.*;
import com.officefood.healthy_food_api.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl extends CrudServiceImpl<Inventory> implements InventoryService {
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final BowlRepository bowlRepository;
    private final BowlItemRepository bowlItemRepository;
    private final InventoryRepository repository;

    @Override @Transactional(readOnly = true)
    public double getBalance(java.util.UUID storeId, java.util.UUID ingredientId) {
        Double net = inventoryRepository.getNetChange(storeId, ingredientId);
        return net == null ? 0d : net;
    }

    @Override public void reserveForOrder(java.util.UUID orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        java.util.List<Bowl> bowls = bowlRepository.findByOrderId(orderId);
        for (Bowl b : bowls) {
            for (BowlItem it : bowlItemRepository.findByBowlId(b.getId())) {
                Inventory inv = new Inventory();
                inv.setStore(order.getStore());
                inv.setIngredient(it.getIngredient());
                inv.setAction(StockAction.RESERVATION);
                inv.setQuantityChange(-1 * (it.getQuantity() == null ? 0d : it.getQuantity()));
                inv.setRefOrderId(order.getId());
                inventoryRepository.save(inv);
            }
        }
    }

    @Override public void consumeForOrder(java.util.UUID orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        java.util.List<Bowl> bowls = bowlRepository.findByOrderId(orderId);
        for (Bowl b : bowls) {
            for (BowlItem it : bowlItemRepository.findByBowlId(b.getId())) {
                Inventory inv = new Inventory();
                inv.setStore(order.getStore());
                inv.setIngredient(it.getIngredient());
                inv.setAction(StockAction.CONSUMPTION);
                inv.setQuantityChange(-1 * (it.getQuantity() == null ? 0d : it.getQuantity()));
                inv.setRefOrderId(order.getId());
                inventoryRepository.save(inv);
            }
        }
    }

    @Override public void releaseReservationForOrder(java.util.UUID orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        java.util.List<Bowl> bowls = bowlRepository.findByOrderId(orderId);
        for (Bowl b : bowls) {
            for (BowlItem it : bowlItemRepository.findByBowlId(b.getId())) {
                Inventory inv = new Inventory();
                inv.setStore(order.getStore());
                inv.setIngredient(it.getIngredient());
                inv.setAction(StockAction.RETURN_IN);
                inv.setQuantityChange(it.getQuantity() == null ? 0d : it.getQuantity());
                inv.setRefOrderId(order.getId());
                inventoryRepository.save(inv);
            }
        }
    }

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<Inventory, java.util.UUID> repo() {
        return repository;
    }
}
