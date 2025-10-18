package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.Inventory;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface InventoryRepository extends JpaRepository<Inventory, java.util.UUID>, InventoryRepositoryCustom {
    @Query("select coalesce(sum(i.quantityChange),0) from Inventory i where i.store.id = :storeId and i.ingredient.id = :ingredientId")
    Double getNetChange(@Param("storeId") java.util.UUID storeId, @Param("ingredientId") java.util.UUID ingredientId);

    List<Inventory> findByStoreIdAndIngredientIdOrderByCreatedAtAsc(java.util.UUID storeId, java.util.UUID ingredientId);

    @Query("select coalesce(sum(i.quantityChange),0) from Inventory i where i.store.id = :storeId and i.ingredient.id = :ingredientId")
    Double getBalance(@Param("storeId") java.util.UUID storeId, @Param("ingredientId") java.util.UUID ingredientId);
}
