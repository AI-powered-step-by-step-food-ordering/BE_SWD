package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.dto.request.PromotionSearchRequest;
import com.officefood.healthy_food_api.model.Promotion;
import com.officefood.healthy_food_api.repository.PromotionRepository;
import com.officefood.healthy_food_api.service.PromotionService;
import com.officefood.healthy_food_api.specification.PromotionSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PromotionServiceImpl extends CrudServiceImpl<Promotion> implements PromotionService {
    private final PromotionRepository repository;

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<Promotion, String> repo() {
        return repository;
    }

    @Override public void activatePromotion(String code) { /* TODO */ }
    @Override public void deactivatePromotion(String code) { /* TODO */ }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<Promotion> search(PromotionSearchRequest searchRequest) {
        log.info("Searching promotions with criteria: {}", searchRequest);

        // Build specification from search request
        Specification<Promotion> spec = PromotionSpecifications.withSearchCriteria(searchRequest);

        // Execute search
        java.util.List<Promotion> promotions = repository.findAll(spec);

        log.info("Found {} promotions matching search criteria", promotions.size());
        return promotions;
    }

}



