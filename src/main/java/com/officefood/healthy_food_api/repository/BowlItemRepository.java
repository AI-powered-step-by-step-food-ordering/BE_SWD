package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.BowlItem;
import com.officefood.healthy_food_api.repository.base.UuidJpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BowlItemRepository extends UuidJpaRepository<BowlItem> {

    // Get all bowl items with ingredient and category
    @Query("SELECT DISTINCT bi FROM BowlItem bi " +
           "LEFT JOIN FETCH bi.ingredient i " +
           "LEFT JOIN FETCH i.category c " +
           "WHERE bi.isActive = true " +
           "ORDER BY bi.createdAt DESC")
    List<BowlItem> findAllWithIngredient();

    // Get bowl item by ID with ingredient and category
    @Query("SELECT bi FROM BowlItem bi " +
           "LEFT JOIN FETCH bi.ingredient i " +
           "LEFT JOIN FETCH i.category c " +
           "WHERE bi.id = :id " +
           "AND bi.isActive = true")
    Optional<BowlItem> findByIdWithIngredient(@Param("id") String id);
}
