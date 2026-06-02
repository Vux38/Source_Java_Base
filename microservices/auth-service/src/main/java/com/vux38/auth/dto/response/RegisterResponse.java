package com.vux38.auth.dto.response;

/**
 * Response body returned after a user account is registered.
 */
public record RegisterResponse(Long id, String username, String email, String status) {
}
