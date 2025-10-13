package com.officefood.healthy_food_api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API response wrapper")
public class ApiResponse<T> {

    @Schema(description = "Request success flag", example = "true")
    private boolean success;

    @Schema(description = "Status code", example = "200")
    private Integer code;

    @Schema(description = "Human readable message", example = "OK")
    private String message;

    @Schema(description = "Payload data")
    private T data;

    @Schema(description = "Error code identifier", example = "VALIDATION_ERROR")
    private String errorCode;

    @Schema(description = "Timestamp")
    private LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(Integer code, String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(code)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(Integer code, String errorCode, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .errorCode(errorCode)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(Integer code, String errorCode, String message, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .errorCode(errorCode)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
}


