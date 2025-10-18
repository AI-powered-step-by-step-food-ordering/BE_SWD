package com.officefood.healthy_food_api.mapper;

import com.officefood.healthy_food_api.dto.request.BowlTemplateRequest;
import com.officefood.healthy_food_api.dto.response.BowlTemplateResponse;
import com.officefood.healthy_food_api.model.*;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class)
public interface BowlTemplateMapper {
    @Mapping(target = "id", ignore = true)

    @Mapping(target = "steps", ignore = true)
    @Mapping(target = "bowls", ignore = true)
    BowlTemplate toEntity(BowlTemplateRequest req);
    BowlTemplateResponse toResponse(BowlTemplate entity);
}
