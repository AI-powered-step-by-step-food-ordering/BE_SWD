package com.officefood.healthy_food_api.specification;

import com.officefood.healthy_food_api.dto.request.BowlTemplateSearchRequest;
import com.officefood.healthy_food_api.model.BowlTemplate;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BowlTemplateSpecifications {

    public static Specification<BowlTemplate> withSearchCriteria(BowlTemplateSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Exact match - templateId
            if (request.getTemplateId() != null && !request.getTemplateId().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("id"), request.getTemplateId().trim()));
            }

            // Partial match - name (case-insensitive)
            if (request.getName() != null && !request.getName().trim().isEmpty()) {
                String searchPattern = "%" + request.getName().trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        searchPattern
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

