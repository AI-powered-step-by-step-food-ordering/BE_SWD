package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.model.BowlTemplate;

import java.util.List;
import java.util.Optional;

public interface BowlTemplateService extends CrudService<BowlTemplate> {
    void publishTemplate(String templateId);
    List<BowlTemplate> findAllWithSteps();
    Optional<BowlTemplate> findByIdWithSteps(String id);
    List<BowlTemplate> findAllTemplatesWithCompleteDefaults();
}
