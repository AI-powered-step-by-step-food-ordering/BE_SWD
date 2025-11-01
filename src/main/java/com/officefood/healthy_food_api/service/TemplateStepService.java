package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.model.TemplateStep;

public interface TemplateStepService extends CrudService<TemplateStep> {
    void moveStep(String stepId, int newIndex);
}
