package vux38.auth.basic.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vux38.auth.basic.dto.request.LoginRequest;
import vux38.auth.basic.dto.response.AuthResponse;
import vux38.auth.basic.entity.AuthProvider;
import vux38.auth.basic.jwt.JwtService;
import vux38.auth.basic.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class GoogleAuthStrategy implements AuthenticationStrategy {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public boolean supports(AuthProvider provider) {
        return provider == AuthProvider.GOOGLE;
    }

    @Override
    public AuthResponse authenticate(LoginRequest request) {
        // Google login đi qua OAuth2 callback, không qua đây
        // Xử lý tại OAuth2LoginSuccessHandler
        throw new UnsupportedOperationException("Google login không đi qua endpoint này");
    }
}