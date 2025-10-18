package com.officefood.healthy_food_api.mapper;

import com.officefood.healthy_food_api.dto.request.BowlRequest;
import com.officefood.healthy_food_api.dto.response.BowlResponse;
import com.officefood.healthy_food_api.model.*;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class)
public interface BowlMapper {
    @Mapping(target = "id", ignore = true)

    @Mapping(target = "order", expression = "java(com.officefood.healthy_food_api.mapper.helpers.OrderMapperHelper.order(req.getOrderId()))")
    @Mapping(target = "template", expression = "java(com.officefood.healthy_food_api.mapper.helpers.BowlTemplateMapperHelper.bowlTemplate(req.getTemplateId()))")
    @Mapping(target = "items", ignore = true)
    Bowl toEntity(BowlRequest req);
    BowlResponse toResponse(Bowl entity);
}
