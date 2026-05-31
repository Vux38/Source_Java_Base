package com.vux38.base.common.response;

import lombok.Builder;
import lombok.Data;

/**
 * Generic API Response Wrapper
 *
 * <p>
 * Standard response format for all APIs.
 * </p>
 *
 * @param <T> data type
 *
 * @author Vux38
 */
@Data
@Builder
public class BaseResponse<T> {

    /**
     * HTTP status code
     */
    private int status;

    /**
     * Message for client
     */
    private String message;

    /**
     * Actual response data
     */
    private T data;

    // ================= STATIC FACTORY =================

    public static <T> BaseResponse<T> success(T data) {
        return BaseResponse.<T>builder()
                .status(200)
                .message("Success")
                .data(data)
                .build();
    }

    public static <T> BaseResponse<T> success(String message, T data) {
        return BaseResponse.<T>builder()
                .status(200)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> BaseResponse<T> error(int status, String message) {
        return BaseResponse.<T>builder()
                .status(status)
                .message(message)
                .data(null)
                .build();
    }
}