package com.vux38.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for logout operation.
 *
 * @param refreshToken the refresh token to invalidate
 * @author VUX38
 * @version 1.0
 * @since 2026
 */
public record LogoutRequest(
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {}