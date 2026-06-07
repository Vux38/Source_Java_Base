package com.vux38.auth.controller;

import com.vux38.auth.dto.request.LoginRequest;
import com.vux38.auth.dto.request.RefreshTokenRequest;
import com.vux38.auth.dto.request.RegisterRequest;
import com.vux38.auth.dto.response.AuthResponse;
import com.vux38.auth.dto.response.HealthResponse;
import com.vux38.auth.service.AuthService;
import com.vux38.auth.service.RefreshTokenService;
import com.vux38.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthService authService, RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
    }

    @GetMapping("/health")
    public ApiResponse<HealthResponse> health(HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                "Auth service is running",
                new HealthResponse("auth-service", "UP"),
                traceId(httpRequest)
        );
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        log.info("Register request received for username: {}", request.username());
        AuthResponse response = authService.register(request);
        return ApiResponse.ok(
                "User registered successfully",
                response,
                traceId(httpRequest)
        );
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        log.info("Login request received for username: {}", request.username());
        AuthResponse response = authService.login(request);
        return ApiResponse.ok(
                "Login successful",
                response,
                traceId(httpRequest)
        );
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest
    ) {
        log.info("Refresh token request received");
        AuthResponse response = authService.refresh(request);
        return ApiResponse.ok(
                "Token refreshed successfully",
                response,
                traceId(httpRequest)
        );
    }

    /**
     * Logout endpoint - nhận refresh token từ Bearer header (chuẩn OAuth2)
     *
     * Cách dùng đúng:
     * Authorization: Bearer <refresh_token>
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest httpRequest
    ) {
        log.info("Logout request received");

        // Extract refresh token từ Bearer header
        String refreshToken = extractRefreshToken(authHeader);

        if (refreshToken != null && !refreshToken.isEmpty()) {
            refreshTokenService.logout(refreshToken);
            log.info("User logged out successfully with refresh token");
        } else {
            log.warn("Logout request without refresh token in Authorization header");
        }

        return ApiResponse.ok(
                "Logged out successfully. Please discard your tokens.",
                null,
                traceId(httpRequest)
        );
    }

    /**
     * Alternative: Logout với cả access token và refresh token
     * Access token trong Bearer header, refresh token trong header riêng
     */
    @PostMapping("/logout/v2")
    public ApiResponse<Void> logoutV2(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Refresh-Token", required = false) String refreshTokenHeader,
            HttpServletRequest httpRequest
    ) {
        log.info("Logout V2 request received");

        String accessToken = extractToken(authHeader);
        String refreshToken = refreshTokenHeader;

        // Priority: refresh token from header > refresh token from access token
        if (refreshToken == null && accessToken != null) {
            // Try to extract refresh token from database using access token
            refreshToken = refreshTokenService.findRefreshTokenByAccessToken(accessToken);
        }

        if (refreshToken != null) {
            refreshTokenService.logout(refreshToken);
            log.info("User logged out successfully");
        }

        return ApiResponse.ok(
                "Logged out successfully",
                null,
                traceId(httpRequest)
        );
    }

    /**
     * Logout from all devices - dùng access token để xác định user
     */
    @PostMapping("/logout/all")
    public ApiResponse<Void> logoutAllDevices(
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest httpRequest
    ) {
        String accessToken = extractToken(authHeader);
        log.info("Logout all devices request received");

        if (accessToken != null) {
            String username = authService.extractUsernameFromToken(accessToken);
            if (username != null) {
                refreshTokenService.logoutAllDevices(username);
                log.info("User logged out from all devices: {}", username);
            }
        }

        return ApiResponse.ok(
                "Logged out from all devices successfully",
                null,
                traceId(httpRequest)
        );
    }

    /**
     * Admin logout - force logout specific user
     */
    @PostMapping("/logout/user/{userId}")
    public ApiResponse<Void> logoutUserById(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest httpRequest
    ) {
        // Check admin permission here
        log.info("Admin logout request for user id: {}", userId);
        refreshTokenService.logoutByUserId(userId);

        return ApiResponse.ok(
                "User logged out successfully",
                null,
                traceId(httpRequest)
        );
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Extract refresh token from Authorization header
     * Format: Authorization: Bearer <refresh_token>
     */
    private String extractRefreshToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Extract token from Authorization header
     */
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private String traceId(HttpServletRequest request) {
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isEmpty()) {
            traceId = request.getHeader("Trace-Id");
        }
        return traceId;
    }
}
