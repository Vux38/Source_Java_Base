package com.vux38.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for username and password login.
 */
public record LoginRequest(
        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Password is required")
        String password
) {
}
