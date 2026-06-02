package com.vux38.auth.dto.response;

/**
 * Response body returned by the health endpoint.
 */
public record HealthResponse(String service, String status) {
}
