package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.BowlItem;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface BowlItemRepository extends JpaRepository<BowlItem, java.util.UUID>, BowlItemRepositoryCustom {
    List<BowlItem> findByBowlId(java.util.UUID bowlId);

    @Query("select count(it) from BowlItem it where it.bowl.order.id = :orderId and it.ingredient.id = :ingredientId")
    long countIngredientInOrder(@Param("orderId") java.util.UUID orderId, @Param("ingredientId") java.util.UUID ingredientId);
}
