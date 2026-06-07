package com.auth.project.service;

import com.auth.project.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse getProfile(String username);
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    void assignRole(Long userId, String roleName);
    void revokeRole(Long userId, String roleName);
    void toggleUserStatus(Long userId);
}
