package com.vux38.auth.dto.request;

import lombok.Builder;

@Builder
public record CreateUserProfileRequest(
        Long userId,
        String username,
        String fullname,
        String email
) {}