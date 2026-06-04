package com.vux38.user.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final List<UserData> USERS = List.of(
            new UserData(1L, "admin", "admin@vux38.com", "ADMIN"),
            new UserData(2L, "customer", "customer@vux38.com", "CUSTOMER"));

    @GetMapping("/health")
    public ApiResponse<HealthData> health() {
        return ApiResponse.ok(
                "User service is running",
                new HealthData("user-service", "UP"));
    }

    @GetMapping
    public ApiResponse<List<UserData>> findAll() {
        return ApiResponse.ok("Users loaded successfully", USERS);
    }

    @GetMapping("/{id}")
    public ApiResponse<UserData> findById(@PathVariable("id") Long id) {
        UserData user = USERS.stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        return ApiResponse.ok("User loaded successfully", user);
    }


    public record HealthData(String service, String status) {
    }

    public record UserData(Long id, String username, String email, String role) {
    }
}
