package com.vux38.base.module.user.service.impl;

import com.vux38.base.module.user.dto.request.CreateUserRequest;
import com.vux38.base.module.user.dto.response.UserResponse;
import com.vux38.base.module.user.entity.Role;
import com.vux38.base.module.user.entity.User;
import com.vux38.base.module.user.mapper.UserMapper;
import com.vux38.base.module.user.repository.RoleRepository;
import com.vux38.base.module.user.repository.UserRepository;
import com.vux38.base.module.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

/**
 * Implementation of UserService.
 *
 * Handles user business logic, validation, and persistence.
 *
 * @author Vux38
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create new user with default role USER
     */
    @Override
    public UserResponse createUser(CreateUserRequest request) {

        validateCreateUser(request);

        Role defaultRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Role USER not found"));

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .roles(Set.of(defaultRole))
                .active(true)
                .build();

        userRepository.save(user);

        log.info("User created: {}", user.getEmail());

        return UserMapper.toResponse(user);
    }

    /**
     * Get user by ID
     */
    @Override
    public UserResponse getById(Long id) {
        User user = findUserOrThrow(id);
        return UserMapper.toResponse(user);
    }

    /**
     * Get all users
     */
    @Override
    public List<UserResponse> getAll() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    /**
     * Delete user (soft delete)
     */
    @Override
    public void delete(Long id) {
        User user = findUserOrThrow(id);

        user.setActive(false); // soft delete
        userRepository.save(user);

        log.warn("User soft-deleted: {}", user.getEmail());
    }

    // ================= PRIVATE METHODS =================

    /**
     * Validate create user request
     */
    private void validateCreateUser(CreateUserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (request.getPassword().length() < 6) {
            throw new RuntimeException("Password too weak");
        }
    }

    /**
     * Find user or throw exception
     */
    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}