package com.officefood.healthy_food_api.mapper;

import com.officefood.healthy_food_api.dto.request.BowlItemRequest;
import com.officefood.healthy_food_api.dto.response.BowlItemResponse;
import com.officefood.healthy_food_api.model.*;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class, uses = {IngredientMapper.class})
public interface BowlItemMapper {
    @IgnoreBaseEntityFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "unitPrice", ignore = true) // Will be set by service from Ingredient (snapshot)
    @Mapping(target = "bowl", expression = "java(com.officefood.healthy_food_api.mapper.helpers.BowlMapperHelper.bowl(req.getBowlId()))")
    @Mapping(target = "ingredient", expression = "java(com.officefood.healthy_food_api.mapper.helpers.IngredientMapperHelper.ingredient(req.getIngredientId()))")
    BowlItem toEntity(BowlItemRequest req);

    @Mapping(target = "bowlId", source = "bowl.id")
    @Mapping(target = "ingredientId", source = "ingredient.id")
    @Mapping(target = "ingredient", source = "ingredient")
    BowlItemResponse toResponse(BowlItem entity);
}
