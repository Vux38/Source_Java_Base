package com.vux38.base.common.exception;

import com.vux38.base.common.response.ApiResponse;
import com.vux38.base.common.response.ResponseBuilder;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<?> handle(RuntimeException ex) {
        return ResponseBuilder.error(
                ErrorCode.VALIDATION_ERROR.getCode(),
                ex.getMessage()
        );
    }
}