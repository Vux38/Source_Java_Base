package com.vux38.user.controller;

import java.util.List;

import com.vux38.common.constant.Headers;
import com.vux38.common.response.ApiResponse;
import com.vux38.common.response.HealthData;
import com.vux38.user.dto.response.UserResponse;
import com.vux38.user.entity.UserProfile;
import com.vux38.user.repository.UserProfileRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserProfileRepository userProfileRepository;

    public UserController(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

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
        List<UserResponse> users = userProfileRepository.findAll().stream()
                .map(this::toUserResponse)
                .toList();

        return ApiResponse.ok(
                "Users loaded successfully",
                users,
                traceId(request)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> findById(
            @PathVariable("id") @Min(1) Long id,
            HttpServletRequest request
    ) {
        UserProfile userProfile = userProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        return ApiResponse.ok(
                "User loaded successfully",
                toUserResponse(userProfile),
                traceId(request)
        );
    }

    @GetMapping("/username/{username}")
    public ApiResponse<UserResponse> findByUsername(
            @PathVariable("username") String username,
            HttpServletRequest request
    ) {
        UserProfile userProfile = userProfileRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        return ApiResponse.ok(
                "User loaded successfully",
                toUserResponse(userProfile),
                traceId(request)
        );
    }

    @GetMapping("/email/{email}")
    public ApiResponse<UserResponse> findByEmail(
            @PathVariable("email") String email,
            HttpServletRequest request
    ) {
        UserProfile userProfile = userProfileRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        return ApiResponse.ok(
                "User loaded successfully",
                toUserResponse(userProfile),
                traceId(request)
        );
    }

    private UserResponse toUserResponse(UserProfile profile) {
        return new UserResponse(
                profile.getUserId(),
                profile.getUsername(),
                profile.getEmail(),
                profile.getFullName(),
                profile.getPhone(),
                profile.getAvatar(),
                profile.getBirthday()
        );
    }

    private String traceId(HttpServletRequest request) {
        return request.getHeader(Headers.TRACE_ID);
    }
}