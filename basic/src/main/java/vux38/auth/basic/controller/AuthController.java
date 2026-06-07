package vux38.auth.basic.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vux38.auth.basic.common.ApiResponse;
import vux38.auth.basic.dto.request.LoginRequest;
import vux38.auth.basic.dto.request.RefreshTokenRequest;
import vux38.auth.basic.dto.request.RegisterRequest;
import vux38.auth.basic.dto.response.AuthResponse;
import vux38.auth.basic.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;  // ✅ interface, không phải Impl

    /**
     * POST /api/auth/register
     * Đăng ký tài khoản mới bằng email + password
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse data = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Đăng ký thành công", data));
    }

    /**
     * POST /api/auth/login
     * Đăng nhập, trả về accessToken + refreshToken
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse data = authService.login(request);
        return ResponseEntity.ok(
                ApiResponse.success("Đăng nhập thành công", data));
    }

    /**
     * POST /api/auth/refresh
     * Dùng refreshToken để lấy accessToken mới
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthResponse data = authService.refreshToken(request);
        return ResponseEntity.ok(
                ApiResponse.success("Làm mới token thành công", data));
    }

    /**
     * POST /api/auth/logout
     * Vô hiệu hoá token hiện tại
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String bearerToken) {

        String accessToken = bearerToken.replace("Bearer ", "");
        authService.logout(accessToken);
        return ResponseEntity.ok(
                ApiResponse.success("Đăng xuất thành công"));
    }
}