package com.auth.project.service.impl;

import com.auth.project.dto.response.UserResponse;
import com.auth.project.entity.Role;
import com.auth.project.entity.User;
import com.auth.project.exception.BadRequestException;
import com.auth.project.exception.ResourceNotFoundException;
import com.auth.project.repository.RoleRepository;
import com.auth.project.repository.UserRepository;
import com.auth.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(String username) {
        User user = userRepository.findByUsernameWithRoles(username)
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return mapToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToResponse(user);
    }

    @Override
    @Transactional
    public void assignRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Role role = roleRepository.findByName(parseRoleName(roleName))
            .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));

        if (user.getRoles().contains(role)) {
            throw new BadRequestException("User already has role: " + roleName);
        }
        user.addRole(role);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void revokeRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Role role = roleRepository.findByName(parseRoleName(roleName))
            .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));

        user.removeRole(role);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────
    private UserResponse mapToResponse(User user) {
        List<String> roles = user.getRoles().stream()
            .map(r -> r.getName().name())
            .collect(Collectors.toList());

        return UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .enabled(user.isEnabled())
            .roles(roles)
            .createdAt(user.getCreatedAt())
            .build();
    }

    private Role.RoleName parseRoleName(String name) {
        try {
            String normalized = name.toUpperCase().startsWith("ROLE_") ? name.toUpperCase()
                : "ROLE_" + name.toUpperCase();
            return Role.RoleName.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role name: " + name);
        }
    }
}
