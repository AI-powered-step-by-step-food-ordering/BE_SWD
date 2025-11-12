package com.officefood.healthy_food_api.specification;

import com.officefood.healthy_food_api.dto.request.UserSearchRequest;
import com.officefood.healthy_food_api.model.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecifications {

    public static Specification<User> withSearchCriteria(UserSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Exact match - userId
            if (request.getUserId() != null && !request.getUserId().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("id"), request.getUserId().trim()));
            }

            // Partial match - fullName (case-insensitive)
            if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
                String searchPattern = "%" + request.getFullName().trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("fullName")),
                        searchPattern
                ));
            }

            // Partial match - email (case-insensitive)
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                String searchPattern = "%" + request.getEmail().trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")),
                        searchPattern
                ));
            }

            // Partial match - phone
            if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
                String searchPattern = "%" + request.getPhone().trim() + "%";
                predicates.add(criteriaBuilder.like(root.get("phone"), searchPattern));
            }


            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

