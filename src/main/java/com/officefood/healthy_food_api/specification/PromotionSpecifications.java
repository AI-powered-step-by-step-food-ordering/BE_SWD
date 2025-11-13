package com.officefood.healthy_food_api.specification;

import com.officefood.healthy_food_api.dto.request.PromotionSearchRequest;
import com.officefood.healthy_food_api.model.Promotion;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class PromotionSpecifications {

    public static Specification<Promotion> withSearchCriteria(PromotionSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Exact match - promotionId
            if (request.getPromotionId() != null && !request.getPromotionId().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("id"), request.getPromotionId().trim()));
            }

            // Exact match - code (case-insensitive)
            if (request.getCode() != null && !request.getCode().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.upper(root.get("code")),
                        request.getCode().trim().toUpperCase()
                ));
            }


            // Partial match - name (case-insensitive)
            if (request.getName() != null && !request.getName().trim().isEmpty()) {
                String searchPattern = "%" + request.getName().trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        searchPattern
                ));
            }

            // Computed status search (active, expired, upcoming)
            if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
                OffsetDateTime now = OffsetDateTime.now();
                String status = request.getStatus().trim().toLowerCase();

                switch (status) {
                    case "active":
                        // startsAt <= now AND (endsAt >= now OR endsAt IS NULL)
                        Predicate startedPredicate = criteriaBuilder.or(
                                criteriaBuilder.isNull(root.get("startsAt")),
                                criteriaBuilder.lessThanOrEqualTo(root.get("startsAt"), now)
                        );
                        Predicate notEndedPredicate = criteriaBuilder.or(
                                criteriaBuilder.isNull(root.get("endsAt")),
                                criteriaBuilder.greaterThanOrEqualTo(root.get("endsAt"), now)
                        );
                        predicates.add(criteriaBuilder.and(startedPredicate, notEndedPredicate));
                        break;

                    case "expired":
                        // endsAt < now
                        predicates.add(criteriaBuilder.and(
                                criteriaBuilder.isNotNull(root.get("endsAt")),
                                criteriaBuilder.lessThan(root.get("endsAt"), now)
                        ));
                        break;

                    case "upcoming":
                        // startsAt > now
                        predicates.add(criteriaBuilder.and(
                                criteriaBuilder.isNotNull(root.get("startsAt")),
                                criteriaBuilder.greaterThan(root.get("startsAt"), now)
                        ));
                        break;

                    default:
                        // "all" or unknown - no filter
                        break;
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

