package com.vux38.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for issuing a new access token from a refresh token.
 */
public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}
