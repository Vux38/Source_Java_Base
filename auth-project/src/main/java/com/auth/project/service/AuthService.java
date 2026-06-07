package com.auth.project.service;

import com.auth.project.dto.request.LoginRequest;
import com.auth.project.dto.request.RefreshTokenRequest;
import com.auth.project.dto.request.RegisterRequest;
import com.auth.project.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(Long userId);
}
