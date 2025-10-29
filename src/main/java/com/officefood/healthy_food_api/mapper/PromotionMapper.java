package com.officefood.healthy_food_api.mapper;

import com.officefood.healthy_food_api.dto.request.PromotionRequest;
import com.officefood.healthy_food_api.dto.response.PromotionResponse;
import com.officefood.healthy_food_api.model.*;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class, imports = { com.officefood.healthy_food_api.model.enums.PromotionType.class })
public interface PromotionMapper {
    @IgnoreBaseEntityFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "redemptions", ignore = true)
    @Mapping(target = "type", expression = "java(PromotionType.valueOf(req.getType()))")
    Promotion toEntity(PromotionRequest req);

    PromotionResponse toResponse(Promotion entity);
}
