package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.model.Bowl;

import java.util.List;
import java.util.Optional;

public interface BowlService extends CrudService<Bowl> {
    void markReady(String bowlId);
    List<Bowl> findAllWithTemplateAndSteps();
    Optional<Bowl> findByIdWithTemplateAndSteps(String id);
    Optional<Bowl> findByIdWithTemplateAndItems(String id);
}
