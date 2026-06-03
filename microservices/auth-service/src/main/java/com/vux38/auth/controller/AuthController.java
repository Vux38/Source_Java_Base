package com.vux38.auth.controller;

import com.vux38.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vux38.auth.dto.request.LoginRequest;
import com.vux38.auth.dto.request.RefreshTokenRequest;
import com.vux38.auth.dto.request.RegisterRequest;
import com.vux38.auth.dto.response.AuthResponse;
import com.vux38.auth.dto.response.HealthResponse;
import com.vux38.auth.dto.response.RegisterResponse;
import com.vux38.auth.service.AuthService;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Handles authentication endpoints for registration, login, token refresh, and health checks.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Returns the service health status.
     *
     * @return health response envelope
     */
    @GetMapping("/health")
    public ApiResponse<HealthResponse> health(HttpServletRequest servletRequest) {
        return ApiResponse.ok(
                "Auth service is running",
                new HealthResponse("auth-service", "UP"),
                traceId(servletRequest));
    }

    /**
     * Registers a new user account.
     *
     * @param request validated registration payload
     * @return created user response envelope
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest servletRequest
    ) {
        return ApiResponse.created(
                "User registered successfully",
                authService.register(request),
                traceId(servletRequest));
    }

    /**
     * Authenticates a user with username and password.
     *
     * @param request validated login payload
     * @return access and refresh token response envelope
     */
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest
    ) {

        AuthResponse authResponse = authService.login(request);

        return ApiResponse.ok(
                "Login successfullys",
                authResponse,
                traceId(servletRequest)
        );
    }

    /**
     * Issues a new access token from a valid refresh token.
     *
     * @param request validated refresh token payload
     * @return refreshed token response envelope
     */
    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest servletRequest
    ) {
        return ApiResponse.ok(
                "Token refreshed successfully",
                authService.refresh(request),
                traceId(servletRequest));
    }

    private String traceId(HttpServletRequest servletRequest) {
        return servletRequest.getHeader("X-Trace-Id");
    }
}
