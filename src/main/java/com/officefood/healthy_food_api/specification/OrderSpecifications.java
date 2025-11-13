package com.officefood.healthy_food_api.specification;

import com.officefood.healthy_food_api.dto.request.OrderSearchRequest;
import com.officefood.healthy_food_api.model.Order;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class OrderSpecifications {

    /**
     * Search orders by userId, storeId, fullName (partial), and status
     */
    public static Specification<Order> withSearchCriteria(OrderSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Add JOIN FETCH for user and store to avoid lazy loading issues
            if (query != null) {
                query.distinct(true);
                root.fetch("user", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("store", jakarta.persistence.criteria.JoinType.LEFT);
            }

            // Exact match: User ID
            if (request.getUserId() != null && !request.getUserId().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), request.getUserId().trim()));
            }

            // Exact match: Store ID
            if (request.getStoreId() != null && !request.getStoreId().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("store").get("id"), request.getStoreId().trim()));
            }

            // Partial match: User's Full Name (case-insensitive)
            if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
                String searchPattern = "%" + request.getFullName().trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("user").get("fullName")),
                    searchPattern
                ));
            }

            // Exact match: Order Status
            if (request.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.getStatus()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

