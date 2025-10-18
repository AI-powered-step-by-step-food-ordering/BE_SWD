package com.officefood.healthy_food_api.mapper;

import com.officefood.healthy_food_api.dto.request.StoreRequest;
import com.officefood.healthy_food_api.dto.response.StoreResponse;
import com.officefood.healthy_food_api.model.*;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class, imports = { com.officefood.healthy_food_api.model.enums.StockAction.class })
public interface StoreMapper {
    @Mapping(target = "id", ignore = true)
    Store toEntity(StoreRequest req);
    StoreResponse toResponse(Store entity);
}
