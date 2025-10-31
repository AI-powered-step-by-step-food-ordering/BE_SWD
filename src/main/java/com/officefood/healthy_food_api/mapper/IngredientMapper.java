package com.officefood.healthy_food_api.mapper;

import com.officefood.healthy_food_api.dto.request.IngredientRequest;
import com.officefood.healthy_food_api.dto.response.IngredientResponse;
import com.officefood.healthy_food_api.model.*;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class, imports = { com.officefood.healthy_food_api.model.enums.IngredientKind.class })
public interface IngredientMapper {
    @IgnoreBaseEntityFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", expression = "java(com.officefood.healthy_food_api.mapper.helpers.CategoryMapperHelper.category(req.getCategoryId()))")
    @Mapping(target = "bowlItems", ignore = true)
    @Mapping(target = "inventories", ignore = true)
    @Mapping(target = "primaryRestrictions", ignore = true)
    @Mapping(target = "restrictedBy", ignore = true)
    Ingredient toEntity(IngredientRequest req);

    @Mapping(target = "active", expression = "java(entity.getIsActive())")
    @Mapping(target = "categoryId", source = "category.id")
    IngredientResponse toResponse(Ingredient entity);
}
