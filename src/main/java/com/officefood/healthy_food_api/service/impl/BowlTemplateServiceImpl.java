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

    @Override public void publishTemplate(String templateId) { repository.findById(templateId).orElseThrow(); /* TODO */ }

}
