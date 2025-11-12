package com.officefood.healthy_food_api.specification;

import com.officefood.healthy_food_api.dto.request.OrderSearchRequest;
import com.officefood.healthy_food_api.model.Order;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class OrderSpecifications {

    /**
     * Simplified search - only by userId and storeId
     */
    public static Specification<Order> withSearchCriteria(OrderSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Exact match: User ID
            if (request.getUserId() != null && !request.getUserId().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), request.getUserId().trim()));
            }

            // Exact match: Store ID
            if (request.getStoreId() != null && !request.getStoreId().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("store").get("id"), request.getStoreId().trim()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

