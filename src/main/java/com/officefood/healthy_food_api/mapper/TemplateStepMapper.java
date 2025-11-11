package com.officefood.healthy_food_api.mapper;

import com.officefood.healthy_food_api.dto.request.TemplateStepRequest;
import com.officefood.healthy_food_api.dto.response.CategoryResponse;
import com.officefood.healthy_food_api.dto.response.TemplateStepResponse;
import com.officefood.healthy_food_api.model.*;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TemplateStepMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "template", expression = "java(com.officefood.healthy_food_api.mapper.helpers.BowlTemplateMapperHelper.bowlTemplate(req.getTemplateId()))")
    @Mapping(target = "category", expression = "java(com.officefood.healthy_food_api.mapper.helpers.CategoryMapperHelper.category(req.getCategoryId()))")
    @Mapping(target = "defaultIngredients", source = "defaultIngredients")
    TemplateStep toEntity(TemplateStepRequest req);

    @Mapping(target = "templateId", expression = "java(entityTemplateId(entity))")
    @Mapping(target = "categoryId", expression = "java(entityCategoryId(entity))")
    @Mapping(target = "category", expression = "java(entityCategory(entity))")
    @Mapping(target = "defaultIngredients", expression = "java(mapDefaultIngredients(entity.getDefaultIngredients()))")
    TemplateStepResponse toResponse(TemplateStep entity);

    /**
     * Map defaultIngredients từ entity sang DTO
     * Note: Chỉ map basic info, không enrich với ingredient details
     * Enrich sẽ được làm ở service layer nếu cần
     */
    default List<TemplateStepResponse.DefaultIngredientItemDto> mapDefaultIngredients(
            List<TemplateStep.DefaultIngredientItem> items) {
        if (items == null) return null;

        return items.stream()
            .map(item -> {
                TemplateStepResponse.DefaultIngredientItemDto dto =
                    new TemplateStepResponse.DefaultIngredientItemDto();
                dto.setIngredientId(item.getIngredientId());
                dto.setQuantity(item.getQuantity());
                dto.setIsDefault(item.getIsDefault());
                // ingredientName, unitPrice, unit sẽ được enrich ở service layer
                return dto;
            })
            .collect(Collectors.toList());
    }

    default String entityTemplateId(TemplateStep entity) {
        if (entity == null || entity.getTemplate() == null) return null;
        if (!Hibernate.isInitialized(entity.getTemplate())) {
            return ((HibernateProxy) entity.getTemplate()).getHibernateLazyInitializer().getIdentifier().toString();
        }
        return entity.getTemplate().getId();
    }

    default String entityCategoryId(TemplateStep entity) {
        if (entity == null || entity.getCategory() == null) return null;
        if (!Hibernate.isInitialized(entity.getCategory())) {
            return ((HibernateProxy) entity.getCategory()).getHibernateLazyInitializer().getIdentifier().toString();
        }
        return entity.getCategory().getId();
    }

    default CategoryResponse entityCategory(TemplateStep entity) {
        if (entity == null || entity.getCategory() == null) return null;

        CategoryResponse response = new CategoryResponse();

        if (!Hibernate.isInitialized(entity.getCategory())) {
            // Return minimal response with just ID for lazy-loaded entities
            response.setId(((HibernateProxy) entity.getCategory()).getHibernateLazyInitializer().getIdentifier().toString());
            return response;
        }

        // If initialized, map all properties
        Category cat = entity.getCategory();
        response.setId(cat.getId());
        response.setName(cat.getName());
        response.setKind(cat.getKind() != null ? cat.getKind().name() : null);
        response.setImageUrl(cat.getImageUrl());
        response.setActive(cat.getIsActive());
        response.setCreatedAt(cat.getCreatedAt());

        return response;
    }
}
