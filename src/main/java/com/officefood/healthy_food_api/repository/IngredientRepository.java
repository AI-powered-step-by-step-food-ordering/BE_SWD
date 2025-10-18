package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.Ingredient;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface IngredientRepository extends JpaRepository<Ingredient, java.util.UUID>, IngredientRepositoryCustom {
    List<Ingredient> findByCategoryId(java.util.UUID categoryId);
    List<Ingredient> findByNameContainingIgnoreCase(String name);

    @Query("select i from Ingredient i where i.category.id = :categoryId and i.name like concat('%', :q, '%')")
    List<Ingredient> searchInCategory(@Param("categoryId") java.util.UUID categoryId, @Param("q") String q);

    @Query("select i from Ingredient i where (:cid is null or i.category.id = :cid) and lower(i.name) like lower(concat('%', :q, '%')) order by i.name asc")
    List<Ingredient> advancedSearch(@Param("q") String keyword, @Param("cid") java.util.UUID categoryId, Pageable pageable);
}
