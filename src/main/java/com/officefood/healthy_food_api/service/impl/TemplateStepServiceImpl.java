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
    public TemplateStep create(TemplateStep entity) {
        // Generate UUID for ID (TemplateStep requires manual ID assignment)
        if (entity.getId() == null || entity.getId().isEmpty()) {
            entity.setId(java.util.UUID.randomUUID().toString());
        }
        // defaultIngredients đã được set từ mapper hoặc trực tiếp từ entity
        return repository.save(entity);
    }

    @Override
    public TemplateStep update(String id, TemplateStep entity) {
        TemplateStep existing = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("TemplateStep not found: " + id));

        // Update các fields
        existing.setTemplate(entity.getTemplate());
        existing.setCategory(entity.getCategory());
        existing.setMinItems(entity.getMinItems());
        existing.setMaxItems(entity.getMaxItems());
        existing.setDefaultQty(entity.getDefaultQty());
        existing.setDisplayOrder(entity.getDisplayOrder());
        existing.setDefaultIngredients(entity.getDefaultIngredients()); // Update defaultIngredients

        return repository.save(existing);
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

    @Override
    public void moveStep(String stepId, int newIndex) {
        repository.findById(stepId).orElseThrow();
        /* TODO */
    }
}
