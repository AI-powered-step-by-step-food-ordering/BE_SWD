package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.Category;
import com.officefood.healthy_food_api.model.enums.IngredientKind;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface CategoryRepository extends JpaRepository<Category, java.util.UUID>, CategoryRepositoryCustom {
    List<Category> findByKindAndIsActiveTrueOrderByDisplayOrderAsc(IngredientKind kind);

    @Query("select c from Category c where lower(c.name) like lower(concat('%', :q, '%')) order by c.displayOrder asc")
    List<Category> searchByName(@Param("q") String keyword);
}
