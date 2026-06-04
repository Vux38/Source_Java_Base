package com.vux38.common.response;

import java.time.Instant;
import java.util.UUID;

import org.springframework.http.HttpStatus;

/**
 * Standard API response envelope returned by auth-service endpoints.
 *
 * @param meta response metadata
 * @param data response payload
 * @param <T> payload type
 */
public record ApiResponse<T>(
        Meta meta,
        T data
) {

    /**
     * Creates a successful 200 response.
     *
     * @param message response message
     * @param data response payload
     * @param <T> payload type
     * @return response envelope
     */
    public static <T> ApiResponse<T> ok(
            String message,
            T data,
            String traceId
    ) {
        return new ApiResponse<>(
                new Meta(
                        HttpStatus.OK.value(),
                        message,
                        resolveTraceId(traceId),
                        Instant.now().toEpochMilli()
                ),
                data
        );
    }


    /**
     * Creates a successful 201 response.
     *
     * @param message response message
     * @param data response payload
     * @param <T> payload type
     * @return response envelope
     */
    public static <T> ApiResponse<T> created(String message, T data) {
        return created(message, data, null);
    }

    /**
     * Creates a successful 201 response.
     *
     * @param message response message
     * @param data response payload
     * @param traceId request trace id
     * @param <T> payload type
     * @return response envelope
     */
    public static <T> ApiResponse<T> created(String message, T data, String traceId) {
        return of(HttpStatus.CREATED, message, data, traceId);
    }

    public static <T> ApiResponse<T> badRequest(String message, T data) {
        return badRequest(message, data, null);
    }

    public static <T> ApiResponse<T> badRequest(String message, T data, String traceId) {
        return of(HttpStatus.BAD_REQUEST, message, data, traceId);
    }

    public static <T> ApiResponse<T> unauthorized(String message, T data) {
        return unauthorized(message, data, null);
    }

    public static <T> ApiResponse<T> unauthorized(String message, T data, String traceId) {
        return of(HttpStatus.UNAUTHORIZED, message, data, traceId);
    }

    public static <T> ApiResponse<T> error(HttpStatus status, String message, T data) {
        return error(status, message, data, null);
    }

    public static <T> ApiResponse<T> error(HttpStatus status, String message, T data, String traceId) {
        return of(status, message, data, traceId);
    }

    private static <T> ApiResponse<T> of(HttpStatus status, String message, T data, String traceId) {
        return new ApiResponse<>(
                new Meta(
                        status.value(),
                        message,
                        resolveTraceId(traceId),
                        Instant.now().toEpochMilli()
                ),
                data
        );
    }

    private static String resolveTraceId(String traceId) {
        if (traceId == null || traceId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return traceId;
    }


}
