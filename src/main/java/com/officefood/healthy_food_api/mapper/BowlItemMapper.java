package com.officefood.healthy_food_api.mapper;

import com.officefood.healthy_food_api.dto.request.BowlItemRequest;
import com.officefood.healthy_food_api.dto.response.BowlItemResponse;
import com.officefood.healthy_food_api.model.*;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class)
public interface BowlItemMapper {
    @Mapping(target = "id", ignore = true)

    @Mapping(target = "bowl", expression = "java(com.officefood.healthy_food_api.mapper.helpers.BowlMapperHelper.bowl(req.getBowlId()))")
    @Mapping(target = "ingredient", expression = "java(com.officefood.healthy_food_api.mapper.helpers.IngredientMapperHelper.ingredient(req.getIngredientId()))")
    BowlItem toEntity(BowlItemRequest req);
    BowlItemResponse toResponse(BowlItem entity);
}
