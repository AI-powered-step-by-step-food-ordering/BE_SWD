package com.officefood.healthy_food_api.dto;

import com.officefood.healthy_food_api.model.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "RegisterRequest", description = "Thông tin đăng ký người dùng")
public class RegisterRequest {
    @Schema(example = "Nguyen Van A")

    @JsonProperty("fullName")
    @NotBlank
    @Size(max = 255)
    private String fullName;

    @Schema(example = "user@example.com")
    @Email
    @JsonProperty("email")
    @NotBlank
    @Size(max = 255)
    private String email;

    @Schema(example = "P@ssw0rd!")
    @JsonProperty("password")
    @NotBlank
    @Size(min = 1, max = 100)
    private String password;

    @Schema(example = "P@ssw0rd!")
    @JsonProperty("passwordConfirm")
    @NotBlank
    @Size(min = 1, max = 100)
    private String passwordConfirm;

    @Schema(example = "LOSE_WEIGHT")
    @JsonProperty("goalCode")
    @Size(max = 50)
    private String goalCode;

    private Role role;
}
