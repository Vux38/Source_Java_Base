package com.vux38.base.common.response;

import java.util.UUID;

public class ResponseBuilder {

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .meta(ApiResponse.Meta.builder()
                        .status(200)
                        .message(message)
                        .traceId(generateTraceId())
                        .timestamp(System.currentTimeMillis())
                        .build())
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .meta(ApiResponse.Meta.builder()
                        .status(400)
                        .message(message)
                        .traceId(generateTraceId())
                        .timestamp(System.currentTimeMillis())
                        .build())
                .error(ApiResponse.ErrorBody.builder()
                        .code(code)
                        .detail(message)
                        .build())
                .build();
    }

    private static String generateTraceId() {
        return UUID.randomUUID().toString();
    }
}