package com.vux38.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Response object for authentication operations.
 * <p>
 * Contains JWT access token, refresh token, and basic user information.
 * </p>
 *
 * @param accessToken the JWT access token
 * @param refreshToken the refresh token for obtaining new access tokens
 * @param userId the unique identifier of the authenticated user
 * @param username the username of the authenticated user
 * @author VUX38
 * @version 1.0
 */
@Builder
public record AuthResponse(
        String accessToken,
        String refreshToken,
        Long userId,
        String username
) {}