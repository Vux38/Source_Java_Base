package com.vux38.auth.controller;

import java.time.Instant;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vux38.auth.security.JwtTokenService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtTokenService jwtTokenService;

    public AuthController(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @GetMapping("/health")
    public ApiResponse<HealthData> health() {
        return ApiResponse.ok(
                "Auth service is running",
                new HealthData("auth-service", "UP"));
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RegisterData> register(@RequestBody RegisterRequest request) {
        RegisterData data = new RegisterData(
                UUID.randomUUID().toString(),
                request.username(),
                request.email(),
                "REGISTERED");

        return ApiResponse.created("User registered successfully", data);
    }

    @PostMapping("/login")
    public ApiResponse<LoginData> login(@RequestBody LoginRequest request) {
        String token = jwtTokenService.generateToken(request.username());
        LoginData data = new LoginData(request.username(), token, 300);

        return ApiResponse.ok("Login successfully", data);
    }

    public record ApiResponse<T>(int status, boolean success, String message, T data, String timestamp) {
        public static <T> ApiResponse<T> ok(String message, T data) {
            return new ApiResponse<>(HttpStatus.OK.value(), true, message, data, Instant.now().toString());
        }

        public static <T> ApiResponse<T> created(String message, T data) {
            return new ApiResponse<>(HttpStatus.CREATED.value(), true, message, data, Instant.now().toString());
        }
    }

    public record HealthData(String service, String status) {
    }

    public record RegisterRequest(String username, String email, String password) {
    }

    public record RegisterData(String id, String username, String email, String status) {
    }

    public record LoginRequest(String username, String password) {
    }

    public record LoginData(String username, String accessToken, long expiresInSeconds) {
    }
}
