package com.auth.project.controller;

import com.auth.project.dto.response.ApiResponse;
import com.auth.project.dto.response.UserResponse;
import com.auth.project.security.UserDetailsImpl;
import com.auth.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ─── User endpoints (any authenticated user) ─────────────────────────────

    /**
     * GET /api/user/me
     * Get current authenticated user's profile.
     */
    @GetMapping("/user/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserResponse profile = userService.getProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved", profile));
    }

    // ─── Moderator endpoints ─────────────────────────────────────────────────

    /**
     * GET /api/moderator/users/{id}
     * Get specific user by ID (moderators and admins).
     */
    @GetMapping("/moderator/users/{id}")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved", user));
    }

    // ─── Admin endpoints ─────────────────────────────────────────────────────

    /**
     * GET /api/admin/users
     * List all users (admin only).
     */
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved", users));
    }

    /**
     * POST /api/admin/users/{id}/roles/{roleName}
     * Assign a role to a user (admin only).
     */
    @PostMapping("/admin/users/{id}/roles/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assignRole(
            @PathVariable Long id,
            @PathVariable String roleName) {
        userService.assignRole(id, roleName);
        return ResponseEntity.ok(ApiResponse.success("Role assigned successfully"));
    }

    /**
     * DELETE /api/admin/users/{id}/roles/{roleName}
     * Revoke a role from a user (admin only).
     */
    @DeleteMapping("/admin/users/{id}/roles/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> revokeRole(
            @PathVariable Long id,
            @PathVariable String roleName) {
        userService.revokeRole(id, roleName);
        return ResponseEntity.ok(ApiResponse.success("Role revoked successfully"));
    }

    /**
     * PATCH /api/admin/users/{id}/status
     * Toggle user enabled/disabled status (admin only).
     */
    @PatchMapping("/admin/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleStatus(@PathVariable Long id) {
        userService.toggleUserStatus(id);
        return ResponseEntity.ok(ApiResponse.success("User status updated"));
    }
}
