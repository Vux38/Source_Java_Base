package com.vux38.user.controller;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(IllegalArgumentException exception) {
        return new ErrorResponse(false, exception.getMessage(), null, Instant.now().toString());
    }

    public record ErrorResponse(boolean success, String message, Object data, String timestamp) {
    }
}
