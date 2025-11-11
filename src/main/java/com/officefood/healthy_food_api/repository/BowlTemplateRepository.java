package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.BowlTemplate;
import com.officefood.healthy_food_api.repository.base.UuidJpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BowlTemplateRepository extends UuidJpaRepository<BowlTemplate> {

    // Get all templates with steps and categories
    @Query("SELECT DISTINCT t FROM BowlTemplate t " +
           "LEFT JOIN FETCH t.steps ts " +
           "LEFT JOIN FETCH ts.category c " +
           "WHERE t.isActive = true " +
           "ORDER BY t.createdAt DESC")
    List<BowlTemplate> findAllWithSteps();

    // Get template by ID with steps and categories
    @Query("SELECT t FROM BowlTemplate t " +
           "LEFT JOIN FETCH t.steps ts " +
           "LEFT JOIN FETCH ts.category c " +
           "WHERE t.id = :id " +
           "AND t.isActive = true")
    Optional<BowlTemplate> findByIdWithSteps(@Param("id") String id);
}
