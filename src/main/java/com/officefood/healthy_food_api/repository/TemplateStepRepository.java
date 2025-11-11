package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.TemplateStep;
import com.officefood.healthy_food_api.repository.base.UuidJpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TemplateStepRepository extends UuidJpaRepository<TemplateStep> {

    @Query("SELECT ts FROM TemplateStep ts " +
           "LEFT JOIN FETCH ts.template " +
           "LEFT JOIN FETCH ts.category")
    List<TemplateStep> findAllWithJoins();

    @Query("SELECT ts FROM TemplateStep ts " +
           "LEFT JOIN FETCH ts.template " +
           "LEFT JOIN FETCH ts.category " +
           "WHERE ts.id = :id")
    Optional<TemplateStep> findByIdWithJoins(@Param("id") String id);
}
