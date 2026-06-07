package vux38.auth.basic.service;

import vux38.auth.basic.dto.request.LoginRequest;
import vux38.auth.basic.dto.request.RefreshTokenRequest;
import vux38.auth.basic.dto.request.RegisterRequest;
import vux38.auth.basic.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(String accessToken);
}
