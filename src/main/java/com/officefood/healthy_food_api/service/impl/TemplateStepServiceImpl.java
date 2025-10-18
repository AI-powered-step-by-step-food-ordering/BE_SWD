package com.officefood.healthy_food_api.service.impl;
import com.officefood.healthy_food_api.model.TemplateStep;
import com.officefood.healthy_food_api.repository.TemplateStepRepository;
import com.officefood.healthy_food_api.service.TemplateStepService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TemplateStepServiceImpl extends CrudServiceImpl<TemplateStep> implements TemplateStepService {
    private final TemplateStepRepository repository;

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<TemplateStep, java.util.UUID> repo() {
        return repository;
    }
}