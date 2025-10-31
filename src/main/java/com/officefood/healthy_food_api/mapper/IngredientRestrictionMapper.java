package com.officefood.healthy_food_api.mapper;

import com.officefood.healthy_food_api.dto.request.IngredientRestrictionRequest;
import com.officefood.healthy_food_api.dto.response.IngredientRestrictionResponse;
import com.officefood.healthy_food_api.model.IngredientRestriction;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class)
public interface IngredientRestrictionMapper {
    @IgnoreBaseEntityFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "primaryIngredient", expression = "java(com.officefood.healthy_food_api.mapper.helpers.IngredientMapperHelper.ingredient(req.getPrimaryIngredientId()))")
    @Mapping(target = "restrictedIngredient", expression = "java(com.officefood.healthy_food_api.mapper.helpers.IngredientMapperHelper.ingredient(req.getRestrictedIngredientId()))")
    IngredientRestriction toEntity(IngredientRestrictionRequest req);

    @Mapping(target = "primaryIngredientId", source = "primaryIngredient.id")
    @Mapping(target = "primaryIngredientName", source = "primaryIngredient.name")
    @Mapping(target = "restrictedIngredientId", source = "restrictedIngredient.id")
    @Mapping(target = "restrictedIngredientName", source = "restrictedIngredient.name")
    @Mapping(target = "active", expression = "java(entity.getIsActive())")
    IngredientRestrictionResponse toResponse(IngredientRestriction entity);
}
