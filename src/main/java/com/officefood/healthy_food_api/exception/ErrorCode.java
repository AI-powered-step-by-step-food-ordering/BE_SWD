package com.officefood.healthy_food_api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    SUCCESS(200, HttpStatus.OK, "Success"),
    BAD_REQUEST(400, HttpStatus.BAD_REQUEST, "Bad request"),
    UNAUTHORIZED(401, HttpStatus.UNAUTHORIZED, "Unauthorized"),
    FORBIDDEN(403, HttpStatus.FORBIDDEN, "Forbidden"),
    NOT_FOUND(404, HttpStatus.NOT_FOUND, "Not found"),
    CONFLICT(409, HttpStatus.CONFLICT, "Conflict"),

    VALIDATION_ERROR(1002, HttpStatus.BAD_REQUEST, "Validation error"),
    DATA_INTEGRITY_VIOLATION(1003, HttpStatus.BAD_REQUEST, "Data integrity violation"),
    EMAIL_ALREADY_EXISTS(1004, HttpStatus.CONFLICT, "Email already exists"),
    USER_NOT_FOUND(1005, HttpStatus.NOT_FOUND, "User not found"),
    INVALID_CREDENTIALS(1006, HttpStatus.UNAUTHORIZED, "Invalid credentials"),
    DATABASE_ERROR(1007, HttpStatus.INTERNAL_SERVER_ERROR, "Database error"),
    EMAIL_ALREADY_VERIFIED(1008, HttpStatus.BAD_REQUEST, "Email already verified"),
    EMAIL_SENDING_FAILED(1009, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send email"),
    TOKEN_EXPIRED(1010, HttpStatus.BAD_REQUEST, "Token has expired"),
    UNCATEGORIZED_EXCEPTION(9999, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(int code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public int getHttpStatusCode() {
        return httpStatus.value();
    }
}


