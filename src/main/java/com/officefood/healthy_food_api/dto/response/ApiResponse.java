package com.officefood.healthy_food_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private Integer status;
    private String code;
    private String message;
    private T data;

    // Methods for generic ApiResponse<T>
    public static <T> ApiResponse<T> success(int status, String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus(status);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> error(int status, String code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus(status);
        response.setCode(code);
        response.setMessage(message);
        return response;
    }

    // Error with data (status is set from HttpStatusCode)
    public static <T> ApiResponse<T> error(int code, String codeName, String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus(400); // Default to 400, caller can override via ResponseEntity.status()
        response.setCode(codeName);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    // Overloaded methods for non-generic ApiResponse (for NotificationController)
    public static ApiResponse<Void> success(String message) {
        ApiResponse<Void> response = new ApiResponse<>();
        response.setStatus(200);
        response.setMessage(message);
        return response;
    }

    public static ApiResponse<Object> success(String message, Object data) {
        ApiResponse<Object> response = new ApiResponse<>();
        response.setStatus(200);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    public static ApiResponse<Void> error(String message) {
        ApiResponse<Void> response = new ApiResponse<>();
        response.setStatus(400);
        response.setMessage(message);
        return response;
    }
}

