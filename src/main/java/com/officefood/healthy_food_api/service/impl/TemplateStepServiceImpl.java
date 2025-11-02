package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.model.TemplateStep;
import com.officefood.healthy_food_api.repository.TemplateStepRepository;
import com.officefood.healthy_food_api.service.TemplateStepService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
public class TemplateStepServiceImpl extends CrudServiceImpl<TemplateStep> implements TemplateStepService {
    private final TemplateStepRepository repository;

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<TemplateStep, String> repo() {
        return repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TemplateStep> findAll() {
        return repository.findAllWithJoins();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TemplateStep> findById(String id) {
        return repository.findByIdWithJoins(id);
    }

    @Override public void moveStep(String stepId, int newIndex) { repository.findById(stepId).orElseThrow(); /* TODO */ }

}
