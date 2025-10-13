package com.officefood.healthy_food_api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(name = "UserResponse")
public record UserResponse(
        UUID id,
        String fullName,
        String email,
        String companyName,
        String goalCode,
        OffsetDateTime createdAt
) {}


