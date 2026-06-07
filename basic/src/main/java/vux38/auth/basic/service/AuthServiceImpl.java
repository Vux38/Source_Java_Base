package vux38.auth.basic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vux38.auth.basic.dto.request.LoginRequest;
import vux38.auth.basic.dto.request.RefreshTokenRequest;
import vux38.auth.basic.dto.request.RegisterRequest;
import vux38.auth.basic.dto.response.AuthResponse;
import vux38.auth.basic.entity.User;
import vux38.auth.basic.entity.AuthProvider;
import vux38.auth.basic.entity.Role;
import vux38.auth.basic.jwt.JwtService;
import vux38.auth.basic.repository.UserRepository;
import vux38.auth.basic.strategy.AuthenticationStrategy;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final List<AuthenticationStrategy> strategies; // tất cả strategy được inject tự động

    @Override
    public AuthResponse register(RegisterRequest request) {

        // 1. Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        // 2. Tạo user mới
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .provider(AuthProvider.LOCAL)
                .role(Role.ROLE_USER)
                .build();

        userRepository.save(user);

        // 3. Sinh token
        String accessToken  = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        // 1. Chọn đúng strategy theo provider
        AuthenticationStrategy strategy = strategies.stream()
                .filter(s -> s.supports(request.getProvider()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Provider không được hỗ trợ: " + request.getProvider()));

        // 2. Uỷ quyền xác thực cho strategy
        return strategy.authenticate(request);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {

        String token = request.getRefreshToken();

        // 1. Validate refresh token
        if (!jwtService.isTokenValid(token)) {
            throw new RuntimeException("Refresh token không hợp lệ hoặc đã hết hạn");
        }

        // 2. Lấy user từ token
        String email = jwtService.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        // 3. Sinh access token mới
        String newAccessToken = jwtService.generateAccessToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(token) // giữ nguyên refresh token cũ
                .build();
    }

    @Override
    public void logout(String accessToken) {
        // Blacklist token — sẽ implement sau khi có Redis hoặc token store
        // jwtBlacklistService.blacklist(accessToken);
    }
}