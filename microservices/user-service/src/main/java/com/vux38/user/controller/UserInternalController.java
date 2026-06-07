package com.vux38.user.controller;

import com.vux38.common.response.ApiResponse;
import com.vux38.user.dto.request.CreateUserProfileRequest;
import com.vux38.user.entity.UserProfile;
import com.vux38.user.repository.UserProfileRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/internal/users")
@RequiredArgsConstructor
public class UserInternalController {

    private final UserProfileRepository userProfileRepository;

    @PostMapping("/profiles")
    public ApiResponse<Void> createUserProfile(@Valid @RequestBody CreateUserProfileRequest request) {
        log.info("Creating user profile for user ID: {}", request.getUserId());

        // Check if profile already exists
        if (userProfileRepository.existsById(request.getUserId())) {
            throw new RuntimeException("User profile already exists for ID: " + request.getUserId());
        }

        // Check if username exists
        if (userProfileRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }

        // Check if email exists
        if (userProfileRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        // Create new profile
        UserProfile profile = new UserProfile();
        profile.setUserId(request.getUserId());
        profile.setUsername(request.getUsername());
        profile.setFullName(request.getFullname());
        profile.setEmail(request.getEmail());

        userProfileRepository.save(profile);
        log.info("User profile created successfully for user: {}", request.getUsername());

        return ApiResponse.ok("User profile created successfully", null, null);
    }

    @GetMapping("/profiles/{userId}")
    public ApiResponse<UserProfile> getUserProfile(@PathVariable Long userId) {
        log.info("Getting user profile for user ID: {}", userId);

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found: " + userId));

        return ApiResponse.ok("User profile found", profile, null);
    }
}