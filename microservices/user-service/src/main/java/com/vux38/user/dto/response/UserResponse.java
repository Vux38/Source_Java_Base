package com.vux38.user.dto.response;

import java.time.LocalDate;

public record UserResponse(
        Long id,
        String username,
        String email,
        String fullName,
        String phone,
        String avatar,
        LocalDate birthday
) {
    // Constructor cho backward compatibility (khi không có phone, avatar, birthday)
    public UserResponse(Long id, String username, String email, String fullName) {
        this(id, username, email, fullName, null, null, null);
    }
}