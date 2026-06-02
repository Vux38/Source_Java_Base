package com.vux38.auth.dto.response;

/**
 * Response body returned after authentication or token refresh succeeds.
 */
public record AuthResponse(String username, String accessToken, String refreshToken, long expiresInSeconds) {
}
