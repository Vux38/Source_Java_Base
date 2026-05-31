package com.vux38.base.module.user.service;

import com.vux38.base.module.user.dto.request.CreateUserRequest;
import com.vux38.base.module.user.dto.response.UserResponse;

import java.util.List;

/**
 * User service interface.
 *
 * Defines business operations for user management.
 *
 * @author Vux38
 */
public interface UserService {

    /**
     * Create new user
     */
    UserResponse createUser(CreateUserRequest request);

    /**
     * Get user by ID
     */
    UserResponse getById(Long id);

    /**
     * Get all users
     */
    List<UserResponse> getAll();

    /**
     * Delete user (soft delete or hard delete)
     */
    void delete(Long id);
}