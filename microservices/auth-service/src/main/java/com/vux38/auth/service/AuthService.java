package com.vux38.auth.service;

import com.vux38.auth.client.UserServiceClient;  // ← Import Feign Client
import com.vux38.auth.dto.request.CreateUserProfileRequest;  // ← DTO riêng
import com.vux38.auth.dto.request.LoginRequest;
import com.vux38.auth.dto.request.RefreshTokenRequest;
import com.vux38.auth.dto.request.RegisterRequest;
import com.vux38.auth.dto.response.AuthResponse;
import com.vux38.auth.entity.RefreshToken;
import com.vux38.auth.entity.Role;
import com.vux38.auth.entity.User;
import com.vux38.auth.exception.DuplicateUsernameException;
import com.vux38.auth.exception.TokenException;
import com.vux38.auth.exception.UserCreationException;
import com.vux38.auth.repository.RoleRepository;
import com.vux38.auth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@Transactional(readOnly = true)
public class AuthService {

    private static final String DEFAULT_ROLE_NAME = "ROLE_USER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserServiceClient userServiceClient;  // ← Feign Client

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            UserServiceClient userServiceClient  // ← Inject Feign Client
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.userServiceClient = userServiceClient;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("📝 Registering new user with username: {}", request.username());

        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateUsernameException("Username already exists: " + request.username());
        }

        User savedUser = createAuthUser(request);

        CreateUserProfileRequest profileRequest = CreateUserProfileRequest.builder()
                .userId(savedUser.getId())
                .username(request.username())
                .fullname(request.fullname())
                .email(request.email())
                .build();

        try {
            // Gọi qua HTTP đến user-service
            userServiceClient.createUserProfile(profileRequest);
            log.info("User profile created for: {}", savedUser.getUsername());
        } catch (Exception e) {
            log.error("Failed to create user profile, rolling back...", e);
            userRepository.delete(savedUser);
            throw new UserCreationException("Failed to create user profile", e);
        }

        return generateAuthResponse(savedUser);
    }
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("🔐 Login attempt for username: {}", request.username());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = (User) authentication.getPrincipal();

            String accessToken = jwtService.generateToken(user);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            log.info("User logged in successfully: {}", user.getUsername());

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .userId(user.getId())
                    .username(user.getUsername())
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("Login failed for username: {} - Invalid credentials", request.username());
            throw new IllegalArgumentException("Invalid username or password");
        } catch (Exception e) {
            log.error("Login failed for username: {}", request.username(), e);
            throw new RuntimeException("Authentication failed", e);
        }
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        log.info("🔄 Refresh token request received");

        RefreshToken refreshToken = refreshTokenService.verify(request.refreshToken());
        User user = refreshToken.getUser();

        String newAccessToken = jwtService.generateToken(user);

        log.info("New access token issued for user: {}", user.getUsername());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken.getToken())
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        log.info("🚪 Logout request received");

        if (refreshToken != null && !refreshToken.isEmpty()) {
            refreshTokenService.logout(refreshToken);
            log.info("User logged out successfully");
        }

        SecurityContextHolder.clearContext();
    }

    public String validateToken(String token) {
        log.debug("Validating token");

        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new TokenException("User not found with username: " + username));

        if (!jwtService.isTokenValid(token, user)) {
            throw new TokenException("Invalid token");
        }

        return username;
    }

    public String extractUsernameFromToken(String token) {
        return jwtService.extractUsername(token);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new TokenException("User not authenticated");
        }

        return (User) authentication.getPrincipal();
    }

    // ==================== PRIVATE METHODS ====================

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }

    private User createAuthUser(RegisterRequest request) {
        Role defaultRole = roleRepository.findByName(DEFAULT_ROLE_NAME)
                .orElseThrow(() -> new IllegalStateException("Default role not found"));

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEnabled(true);
        user.setRoles(Set.of(defaultRole));

        return userRepository.save(user);
    }

}
