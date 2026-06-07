package vux38.auth.basic.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vux38.auth.basic.dto.request.LoginRequest;
import vux38.auth.basic.dto.response.AuthResponse;
import vux38.auth.basic.entity.AuthProvider;
import vux38.auth.basic.entity.User;
import vux38.auth.basic.jwt.JwtService;
import vux38.auth.basic.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class PasswordAuthStrategy implements AuthenticationStrategy {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public boolean supports(AuthProvider provider) {
        return provider == AuthProvider.LOCAL;
    }

    /**
     * @param request
     * @return
     */
    @Override
    public AuthResponse authenticate(LoginRequest request) {

        // 1. Tìm user theo email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        // 2. Kiểm tra password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu không chính xác");
        }

        // 3. Sinh token
        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .userInfo(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .avatarUrl(user.getAvatarUrl())
                        .role(user.getRole().name())
                        .build())
                .build();
    }
}