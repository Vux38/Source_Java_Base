package com.auth.project.dto.response;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    @Builder.Default private String tokenType = "Bearer";
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private List<String> roles;
}
