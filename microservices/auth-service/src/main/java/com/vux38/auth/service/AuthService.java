package com.vux38.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vux38.auth.dto.request.LoginRequest;
import com.vux38.auth.dto.request.RefreshTokenRequest;
import com.vux38.auth.dto.request.RegisterRequest;
import com.vux38.auth.dto.response.AuthResponse;
import com.vux38.auth.dto.response.RegisterResponse;
import com.vux38.auth.entity.RefreshToken;
import com.vux38.auth.entity.User;
import com.vux38.auth.repository.UserRepository;

@Service
public class AuthService {

    private static final long ACCESS_TOKEN_TTL_SECONDS = 300L;

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a user after validating unique username and email constraints.
     *
     * @param request validated registration request
     * @return safe registration response without password data
     */
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(
                passwordEncoder.encode(request.password())
        );
        user.setEnabled(true);
        User savedUser = userRepository.save(user);
        return new RegisterResponse(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail(), "REGISTERED");
    }

    /**
     * Authenticates a user and issues access and refresh tokens.
     *
     * @param request validated login request
     * @return token response
     */
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        String accessToken = jwtService.generateAccessToken(user.getUsername());
        RefreshToken refreshToken = refreshTokenService.create(user);

        return new AuthResponse(user.getUsername(), accessToken, refreshToken.getToken(), ACCESS_TOKEN_TTL_SECONDS);
    }

    /**
     * Issues a new access token after validating a refresh token.
     *
     * @param request validated refresh token request
     * @return token response with the same refresh token
     */
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.verify(request.refreshToken());
        User user = refreshToken.getUser();
        String accessToken = jwtService.generateAccessToken(user.getUsername());

        return new AuthResponse(user.getUsername(), accessToken, refreshToken.getToken(), ACCESS_TOKEN_TTL_SECONDS);
    }
}
