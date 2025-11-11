package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.model.BowlTemplate;
import com.officefood.healthy_food_api.repository.BowlTemplateRepository;
import com.officefood.healthy_food_api.service.BowlTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class BowlTemplateServiceImpl extends CrudServiceImpl<BowlTemplate> implements BowlTemplateService {
    private final BowlTemplateRepository repository;

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<BowlTemplate, String> repo() {
        return repository;
    }

    @Override
    public void publishTemplate(String templateId) {
        repository.findById(templateId).orElseThrow(); /* TODO */
    }

    @Override
    public java.util.List<BowlTemplate> findAllWithSteps() {
        return repository.findAllWithSteps();
    }

    @Override
    public java.util.Optional<BowlTemplate> findByIdWithSteps(String id) {
        return repository.findByIdWithSteps(id);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<BowlTemplate> findAllTemplatesWithCompleteDefaults() {
        // Get all templates with steps
        java.util.List<BowlTemplate> allTemplates = repository.findAllWithSteps();

        // Filter templates where AT LEAST ONE step has non-empty defaultIngredients
        return allTemplates.stream()
            .filter(template -> {
                if (template.getSteps() == null || template.getSteps().isEmpty()) {
                    return false; // Template without steps cannot have defaults
                }

                // Check if AT LEAST ONE step has defaultIngredients
                long stepsWithDefaults = template.getSteps().stream()
                    .filter(step -> {
                        java.util.List<com.officefood.healthy_food_api.model.TemplateStep.DefaultIngredientItem> defaultIngredients =
                            step.getDefaultIngredients();
                        return defaultIngredients != null && !defaultIngredients.isEmpty();
                    })
                    .count();

                return stepsWithDefaults > 0;
            })
            .collect(java.util.stream.Collectors.toList());
    }
}
