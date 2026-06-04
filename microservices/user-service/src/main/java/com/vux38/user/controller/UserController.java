package com.vux38.user.controller;

import java.util.List;

import com.vux38.common.constant.Headers;
import com.vux38.common.response.ApiResponse;

import com.vux38.common.response.HealthData;
import com.vux38.user.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final List<UserResponse> USERS = List.of(
            new UserResponse(1L, "admin", "admin@vux38.com", "ADMIN"),
            new UserResponse(2L, "customer", "customer@vux38.com", "CUSTOMER"));

    @GetMapping("/health")
    public ApiResponse<HealthData> health(HttpServletRequest request) {
        return ApiResponse.ok(
                "User service is running",
                new HealthData("user-service", "UP"),
                traceId(request)
        );
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> findAll(HttpServletRequest request) {
        return ApiResponse.ok(
                "Users loaded successfully",
                USERS,
                traceId(request)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> findById(
            @PathVariable("id") Long id,
            HttpServletRequest request
    ) {

        UserResponse user = USERS.stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        return ApiResponse.ok(
                "User loaded successfully",
                user,
                traceId(request)
        );
    }

    private String traceId(HttpServletRequest request) {
        return request.getHeader(Headers.TRACE_ID);
    }


}