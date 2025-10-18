package com.officefood.healthy_food_api.mapper;

import com.officefood.healthy_food_api.dto.request.TemplateStepRequest;
import com.officefood.healthy_food_api.dto.response.TemplateStepResponse;
import com.officefood.healthy_food_api.model.*;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class)
public interface TemplateStepMapper {
    @Mapping(target = "id", ignore = true)

    @Mapping(target = "template", expression = "java(com.officefood.healthy_food_api.mapper.helpers.BowlTemplateMapperHelper.bowlTemplate(req.getTemplateId()))")
    @Mapping(target = "category", expression = "java(com.officefood.healthy_food_api.mapper.helpers.CategoryMapperHelper.category(req.getCategoryId()))")
    TemplateStep toEntity(TemplateStepRequest req);
    TemplateStepResponse toResponse(TemplateStep entity);
}
