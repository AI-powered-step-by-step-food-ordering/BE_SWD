package com.officefood.healthy_food_api.mapper;

import com.officefood.healthy_food_api.dto.request.StoreRequest;
import com.officefood.healthy_food_api.dto.response.StoreResponse;
import com.officefood.healthy_food_api.model.*;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class, imports = { com.officefood.healthy_food_api.model.enums.StockAction.class })
public interface StoreMapper {
    @IgnoreBaseEntityFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "inventories", ignore = true)
    Store toEntity(StoreRequest req);

    @Mapping(target = "active", expression = "java(entity.getIsActive())")
    StoreResponse toResponse(Store entity);
}
