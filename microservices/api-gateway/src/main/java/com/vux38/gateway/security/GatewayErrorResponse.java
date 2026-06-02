package com.vux38.gateway.security;

/**
 * Standard error response body returned by the API gateway.
 *
 * @param status HTTP status code returned to the client
 * @param success always {@code false} for gateway errors
 * @param message human-readable error message
 * @param path request path that produced the error
 * @param traceId correlation identifier for debugging a request end to end
 * @param timestamp ISO-8601 timestamp when the error was produced
 */
public record GatewayErrorResponse(
        int status,
        boolean success,
        String message,
        String path,
        String traceId,
        String timestamp
) {
}
