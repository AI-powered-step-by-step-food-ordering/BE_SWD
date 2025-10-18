package com.officefood.healthy_food_api.mapper;

import com.officefood.healthy_food_api.dto.request.PromotionRedemptionRequest;
import com.officefood.healthy_food_api.dto.response.PromotionRedemptionResponse;
import com.officefood.healthy_food_api.model.*;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class, imports = { com.officefood.healthy_food_api.model.enums.RedemptionStatus.class })
public interface PromotionRedemptionMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "promotion", expression = "java(com.officefood.healthy_food_api.mapper.helpers.PromotionMapperHelper.promotion(req.getPromotionId()))")
    @Mapping(target = "order", expression = "java(com.officefood.healthy_food_api.mapper.helpers.OrderMapperHelper.order(req.getOrderId()))")
    @Mapping(target = "status", expression = "java(RedemptionStatus.valueOf(req.getStatus() == null ? \"APPLIED\" : req.getStatus()))")
    PromotionRedemption toEntity(PromotionRedemptionRequest req);

    PromotionRedemptionResponse toResponse(PromotionRedemption entity);
}
