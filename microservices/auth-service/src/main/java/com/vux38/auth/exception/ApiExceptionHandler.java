package com.vux38.auth.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import jakarta.servlet.http.HttpServletRequest;

/**
 * Converts auth-service exceptions into the standard API response JSON format.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * Handles bean validation failures from request DTOs.
     *
     * @param ex validation exception
     * @return validation errors grouped by request field
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Map<String, String>> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest servletRequest
    ) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        return ApiResponse.badRequest("Validation failed", errors, traceId(servletRequest));
    }

    /**
     * Handles invalid business input such as duplicate username or invalid credentials.
     *
     * @param ex invalid argument exception
     * @return standardized bad request response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest servletRequest
    ) {
        return ApiResponse.badRequest(ex.getMessage(), null, traceId(servletRequest));
    }

    /**
     * Handles unexpected server errors without leaking internal details.
     *
     * @return standardized internal server error response
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse handleUnexpected(HttpServletRequest servletRequest) {
        return ApiResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                null,
                traceId(servletRequest));
    }

    private String traceId(HttpServletRequest servletRequest) {
        return servletRequest.getHeader("X-Trace-Id");
    }
}
