package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.model.TemplateStep;
import com.officefood.healthy_food_api.repository.TemplateStepRepository;
import com.officefood.healthy_food_api.service.TemplateStepService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TemplateStepServiceImpl extends CrudServiceImpl<TemplateStep> implements TemplateStepService {
    private final TemplateStepRepository repository;

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<TemplateStep, UUID> repo() {
        return repository;
    }

    @Override public void moveStep(UUID stepId, int newIndex) { repository.findById(stepId).orElseThrow(); /* TODO */ }

}
