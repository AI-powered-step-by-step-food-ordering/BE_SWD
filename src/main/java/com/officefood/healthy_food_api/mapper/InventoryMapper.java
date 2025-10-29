package com.officefood.healthy_food_api.mapper;

import com.officefood.healthy_food_api.dto.request.InventoryRequest;
import com.officefood.healthy_food_api.dto.response.InventoryResponse;
import com.officefood.healthy_food_api.model.*;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class, imports = { com.officefood.healthy_food_api.model.enums.StockAction.class })
public interface InventoryMapper {
    @IgnoreBaseEntityFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "store", expression = "java(com.officefood.healthy_food_api.mapper.helpers.StoreMapperHelper.store(req.getStoreId()))")
    @Mapping(target = "ingredient", expression = "java(com.officefood.healthy_food_api.mapper.helpers.IngredientMapperHelper.ingredient(req.getIngredientId()))")
    @Mapping(target = "action", expression = "java(StockAction.valueOf(req.getAction()))")
    @Mapping(target = "balanceAfter", ignore = true)
    Inventory toEntity(InventoryRequest req);

    @Mapping(target = "storeId", source = "store.id")
    @Mapping(target = "ingredientId", source = "ingredient.id")
    InventoryResponse toResponse(Inventory entity);
}
