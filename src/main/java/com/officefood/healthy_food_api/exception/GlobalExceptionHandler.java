package com.officefood.healthy_food_api.exception;

import com.officefood.healthy_food_api.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        ApiResponse<Object> response = ApiResponse.error(errorCode.getCode(), errorCode.name(), ex.getMessage());
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();
        try {
            Object target = ex.getBindingResult().getTarget();
            log.warn("Validation failed for {} with errors: {}", target != null ? target.getClass().getSimpleName() : "UnknownTarget", errors);
        } catch (Exception ignored) {}
        String message = errors.isEmpty() ? ErrorCode.VALIDATION_ERROR.getMessage() : String.join(", ", errors);
        ApiResponse<Object> response = ApiResponse.error(
                ErrorCode.VALIDATION_ERROR.getCode(),
                ErrorCode.VALIDATION_ERROR.name(),
                message,
                errors
        );
        return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getHttpStatusCode()).body(response);
    }

    @ExceptionHandler({BindException.class, ConstraintViolationException.class, MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiResponse<Object>> handleBadRequest(Exception ex) {
        ApiResponse<Object> response = ApiResponse.error(ErrorCode.BAD_REQUEST.getCode(), ErrorCode.BAD_REQUEST.name(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler({BadCredentialsException.class, AccessDeniedException.class})
    public ResponseEntity<ApiResponse<Object>> handleAuth(Exception ex) {
        ErrorCode ec = (ex instanceof AccessDeniedException) ? ErrorCode.FORBIDDEN : ErrorCode.UNAUTHORIZED;
        ApiResponse<Object> response = ApiResponse.error(ec.getCode(), ec.name(), ec.getMessage());
        return ResponseEntity.status(ec.getHttpStatusCode()).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String msg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        ErrorCode ec = msg != null && msg.toLowerCase().contains("email") && msg.toLowerCase().contains("duplicate")
                ? ErrorCode.EMAIL_ALREADY_EXISTS : ErrorCode.DATA_INTEGRITY_VIOLATION;
        ApiResponse<Object> response = ApiResponse.error(ec.getCode(), ec.name(), msg);
        return ResponseEntity.status(ec.getHttpStatusCode()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleOthers(Exception ex) {
        log.error("====================================================");
        log.error("❌ UNCATEGORIZED EXCEPTION CAUGHT");
        log.error("====================================================");
        log.error("Exception Type: {}", ex.getClass().getName());
        log.error("Exception Message: {}", ex.getMessage());
        if (ex.getCause() != null) {
            log.error("Caused by: {} - {}", ex.getCause().getClass().getName(), ex.getCause().getMessage());
        }
        log.error("Full Stack Trace:", ex);
        log.error("====================================================");

        ApiResponse<Object> response = ApiResponse.error(
            ErrorCode.UNCATEGORIZED_EXCEPTION.getCode(),
            ErrorCode.UNCATEGORIZED_EXCEPTION.name(),
            ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage()
        );
        return ResponseEntity.status(ErrorCode.UNCATEGORIZED_EXCEPTION.getHttpStatusCode()).body(response);
    }
}
