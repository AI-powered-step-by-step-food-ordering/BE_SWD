package com.officefood.healthy_food_api.mapper;

import com.officefood.healthy_food_api.dto.request.BowlTemplateRequest;
import com.officefood.healthy_food_api.dto.response.BowlTemplateResponse;
import com.officefood.healthy_food_api.model.*;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class, uses = {TemplateStepMapper.class})
public abstract class BowlTemplateMapper {

    @org.springframework.beans.factory.annotation.Autowired
    protected TemplateStepMapper templateStepMapper;

    @IgnoreBaseEntityFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "steps", ignore = true)
    @Mapping(target = "bowls", ignore = true)
    public abstract BowlTemplate toEntity(BowlTemplateRequest req);

    @Mapping(target = "active", expression = "java(entity.getIsActive())")
    @Mapping(target = "steps", ignore = true)
    public abstract BowlTemplateResponse toResponse(BowlTemplate entity);

    @AfterMapping
    protected void mapSteps(@MappingTarget BowlTemplateResponse response, BowlTemplate entity) {
        // Only map steps if they are initialized (fetched from database)
        if (entity.getSteps() != null && org.hibernate.Hibernate.isInitialized(entity.getSteps())) {
            response.setSteps(
                entity.getSteps().stream()
                    .map(templateStepMapper::toResponse)
                    .collect(java.util.stream.Collectors.toList())
            );
        } else {
            response.setSteps(null);
        }
    }
}
