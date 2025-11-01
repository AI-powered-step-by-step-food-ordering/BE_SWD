package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.Bowl;
import com.officefood.healthy_food_api.model.BowlTemplate;
import com.officefood.healthy_food_api.repository.base.UuidJpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BowlRepository extends UuidJpaRepository<Bowl> {

    // Get all bowls with template and template steps joined
    @Query("SELECT DISTINCT b FROM Bowl b " +
           "LEFT JOIN FETCH b.template t " +
           "WHERE b.isActive = true " +
           "AND (t.isActive = true OR t IS NULL) " +
           "ORDER BY b.createdAt DESC")
    List<Bowl> findAllWithTemplateAndSteps();

    // Helper query to fetch steps with categories for templates
    @Query("SELECT DISTINCT t FROM BowlTemplate t " +
           "LEFT JOIN FETCH t.steps ts " +
           "LEFT JOIN FETCH ts.category c " +
           "WHERE t.id IN :templateIds " +
           "AND t.isActive = true " +
           "AND (ts.isActive = true OR ts IS NULL)")
    List<BowlTemplate> fetchTemplateSteps(@Param("templateIds") List<String> templateIds);

    // Get bowl by ID with template and template steps joined
    @Query("SELECT b FROM Bowl b " +
           "LEFT JOIN FETCH b.template t " +
           "LEFT JOIN FETCH t.steps ts " +
           "LEFT JOIN FETCH ts.category c " +
           "WHERE b.id = :id " +
           "AND b.isActive = true " +
           "AND (t.isActive = true OR t IS NULL) " +
           "AND (ts.isActive = true OR ts IS NULL)")
    Optional<Bowl> findByIdWithTemplateAndSteps(@Param("id") String id);

    // Get bowl by ID with bowl items (without template steps to avoid Cartesian product)
    @Query("SELECT b FROM Bowl b " +
           "LEFT JOIN FETCH b.template t " +
           "LEFT JOIN FETCH b.items bi " +
           "LEFT JOIN FETCH bi.ingredient i " +
           "LEFT JOIN FETCH i.category ic " +
           "WHERE b.id = :id " +
           "AND b.isActive = true " +
           "AND (t.isActive = true OR t IS NULL) " +
           "AND (bi.isActive = true OR bi IS NULL)")
    Optional<Bowl> findByIdWithTemplateAndItems(@Param("id") String id);
}
