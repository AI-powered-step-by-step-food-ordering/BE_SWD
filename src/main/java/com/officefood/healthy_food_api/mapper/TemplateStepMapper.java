package com.officefood.healthy_food_api.mapper;

import com.officefood.healthy_food_api.dto.request.TemplateStepRequest;
import com.officefood.healthy_food_api.dto.response.CategoryResponse;
import com.officefood.healthy_food_api.dto.response.TemplateStepResponse;
import com.officefood.healthy_food_api.model.*;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class)
public interface TemplateStepMapper {
    @IgnoreBaseEntityFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "template", expression = "java(com.officefood.healthy_food_api.mapper.helpers.BowlTemplateMapperHelper.bowlTemplate(req.getTemplateId()))")
    @Mapping(target = "category", expression = "java(com.officefood.healthy_food_api.mapper.helpers.CategoryMapperHelper.category(req.getCategoryId()))")
    TemplateStep toEntity(TemplateStepRequest req);

    @Mapping(target = "templateId", expression = "java(entityTemplateId(entity))")
    @Mapping(target = "categoryId", expression = "java(entityCategoryId(entity))")
    @Mapping(target = "category", expression = "java(entityCategory(entity))")
    TemplateStepResponse toResponse(TemplateStep entity);

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
