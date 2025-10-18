package com.officefood.healthy_food_api.mapper;

import com.officefood.healthy_food_api.dto.request.CategoryRequest;
import com.officefood.healthy_food_api.dto.response.CategoryResponse;
import com.officefood.healthy_food_api.model.*;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class, imports = { com.officefood.healthy_food_api.model.enums.IngredientKind.class })
public interface CategoryMapper {
    @Mapping(target = "id", ignore = true)

    @Mapping(target = "ingredients", ignore = true)
    @Mapping(target = "templateSteps", ignore = true)
    @Mapping(target = "kind", expression = "java(IngredientKind.valueOf(req.getKind()))")
    Category toEntity(CategoryRequest req);
    CategoryResponse toResponse(Category entity);
}
