package com.officefood.healthy_food_api.mapper;

import com.officefood.healthy_food_api.dto.RegisterRequest;
import com.officefood.healthy_food_api.dto.LoginRequest;
import com.officefood.healthy_food_api.dto.LoginResponse;
import com.officefood.healthy_food_api.dto.EmailVerificationResponse;
import com.officefood.healthy_food_api.model.User;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class, imports = { 
    com.officefood.healthy_food_api.model.enums.Role.class, 
    com.officefood.healthy_food_api.model.enums.AccountStatus.class 
})
public interface AuthMapper {
    
    @IgnoreBaseEntityFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "assignedJobs", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "emailVerificationToken", ignore = true)
    @Mapping(target = "emailVerificationExpiry", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "dateOfBirth", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "role", expression = "java(req.getRole() == null ? Role.USER : req.getRole())")
    @Mapping(target = "status", expression = "java(AccountStatus.PENDING_VERIFICATION)")
    User toEntity(RegisterRequest req);

    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "tokenType", ignore = true)
    @Mapping(target = "expiresIn", ignore = true)
    LoginResponse toLoginResponse(User entity);

    @Mapping(target = "status", expression = "java(entity.getStatus().toString())")
    @Mapping(target = "message", ignore = true)
    EmailVerificationResponse toEmailVerificationResponse(User entity);
}
