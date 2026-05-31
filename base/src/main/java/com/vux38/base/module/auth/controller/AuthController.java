package com.vux38.base.module.auth.controller;


import com.vux38.base.common.response.ApiResponse;
import com.vux38.base.common.response.ResponseBuilder;
import com.vux38.base.module.auth.dto.request.LoginRequest;
import com.vux38.base.module.auth.service.AuthService;
import com.vux38.base.security.tocken.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@RequestBody LoginRequest request) {

        TokenResponse token = authService.login(request);

        return ResponseBuilder.success(token, "Login successful");
    }
}