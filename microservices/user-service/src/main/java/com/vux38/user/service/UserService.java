package com.vux38.user.service;

import com.vux38.user.dto.request.CreateUserProfileRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vux38.user.entity.UserProfile;
import com.vux38.user.repository.UserProfileRepository;

/**
 * Service layer for user profile operations.
 * <p>
 * This service handles CRUD operations for user profiles,
 * including creating, reading, updating, and deleting profile information.
 * </p>
 *
 * @author VUX38
 * @version 1.0
 * @since 2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserProfileRepository userProfileRepository;

    /**
     * Finds a user profile by user ID.
     *
     * @param userId the user ID to search for
     * @return the user profile entity
     * @throws IllegalArgumentException if user profile not found
     */
    public UserProfile findByUserId(Long userId) {
        log.debug("Finding user profile by user ID: {}", userId);

        return userProfileRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User profile not found for user ID: {}", userId);
                    return new IllegalArgumentException("User profile not found: " + userId);
                });
    }

    /**
     * Finds a user profile by username.
     *
     * @param username the username to search for
     * @return the user profile entity
     * @throws IllegalArgumentException if user profile not found
     */
    public UserProfile findByUsername(String username) {
        log.debug("Finding user profile by username: {}", username);

        return userProfileRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User profile not found for username: {}", username);
                    return new IllegalArgumentException("User profile not found: " + username);
                });
    }

    /**
     * Finds a user profile by email.
     *
     * @param email the email to search for
     * @return the user profile entity
     * @throws IllegalArgumentException if user profile not found
     */
    public UserProfile findByEmail(String email) {
        log.debug("Finding user profile by email: {}", email);

        return userProfileRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User profile not found for email: {}", email);
                    return new IllegalArgumentException("User profile not found: " + email);
                });
    }

    /**
     * Checks if a user profile exists for the given user ID.
     *
     * @param userId the user ID to check
     * @return true if profile exists, false otherwise
     */
    public boolean existsByUserId(Long userId) {
        return userProfileRepository.existsById(userId);
    }

    /**
     * Checks if a username already exists.
     *
     * @param username the username to check
     * @return true if username exists, false otherwise
     */
    public boolean existsByUsername(String username) {
        return userProfileRepository.existsByUsername(username);
    }

    /**
     * Checks if an email already exists.
     *
     * @param email the email to check
     * @return true if email exists, false otherwise
     */
    public boolean existsByEmail(String email) {
        return userProfileRepository.existsByEmail(email);
    }

    /**
     * Creates a new user profile.
     *
     * @param profile the user profile entity to create
     * @return the created user profile
     * @throws IllegalArgumentException if email already exists
     */
    @Transactional
    public UserProfile create(UserProfile profile) {
        log.info("Creating user profile for user ID: {}", profile.getUserId());

        if (userProfileRepository.existsByEmail(profile.getEmail())) {
            log.warn("Email already exists: {}", profile.getEmail());
            throw new IllegalArgumentException("Email already exists: " + profile.getEmail());
        }

        if (userProfileRepository.existsByUsername(profile.getUsername())) {
            log.warn("Username already exists: {}", profile.getUsername());
            throw new IllegalArgumentException("Username already exists: " + profile.getUsername());
        }

        UserProfile saved = userProfileRepository.save(profile);
        log.info("User profile created successfully for user ID: {}", saved.getUserId());

        return saved;
    }

    /**
     * Creates a user profile from the create request DTO.
     * <p>
     * This method is called by the auth service via Feign client
     * when a new user registers.
     * </p>
     *
     * @param request the create user profile request
     * @return the created user profile
     * @throws IllegalArgumentException if email or username already exists
     */
    @Transactional
    public UserProfile createUserProfile(CreateUserProfileRequest request) {
        log.info("Creating user profile from request for user ID: {}", request.getUserId());

        // Validate input
        validateCreateRequest(request);

        // Create new user profile entity
        UserProfile profile = new UserProfile();
        profile.setUserId(request.getUserId());
        profile.setUsername(request.getUsername());
        profile.setFullName(request.getFullname());
        profile.setEmail(request.getEmail());

        // Optional fields can be set later via update
        profile.setPhone(null);
        profile.setAvatar(null);
        profile.setBirthday(null);

        UserProfile saved = userProfileRepository.save(profile);
        log.info("User profile created successfully for username: {}", saved.getUsername());

        return saved;
    }

    /**
     * Updates an existing user profile.
     *
     * @param userId the user ID of the profile to update
     * @param request the update request containing new values
     * @return the updated user profile
     * @throws IllegalArgumentException if user profile not found
     */
    @Transactional
    public UserProfile update(Long userId, UserProfile request) {
        log.info("Updating user profile for user ID: {}", userId);

        UserProfile profile = findByUserId(userId);

        // Update only non-null fields
        if (request.getFullName() != null) {
            profile.setFullName(request.getFullName());
        }
        if (request.getEmail() != null) {
            // Check if new email already exists for another user
            if (!profile.getEmail().equals(request.getEmail())
                    && userProfileRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already exists: " + request.getEmail());
            }
            profile.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            profile.setPhone(request.getPhone());
        }
        if (request.getAvatar() != null) {
            profile.setAvatar(request.getAvatar());
        }
        if (request.getBirthday() != null) {
            profile.setBirthday(request.getBirthday());
        }

        UserProfile updated = userProfileRepository.save(profile);
        log.info("User profile updated successfully for user ID: {}", userId);

        return updated;
    }

    /**
     * Partially updates a user profile (only non-null fields).
     *
     * @param userId the user ID of the profile to update
     * @param request the update request containing new values
     * @return the updated user profile
     */
    @Transactional
    public UserProfile patchUpdate(Long userId, UserProfile request) {
        log.info("Partially updating user profile for user ID: {}", userId);
        return update(userId, request);
    }

    /**
     * Updates the email address for a user.
     *
     * @param userId the user ID
     * @param newEmail the new email address
     * @return the updated user profile
     */
    @Transactional
    public UserProfile updateEmail(Long userId, String newEmail) {
        log.info("Updating email for user ID: {}", userId);

        if (userProfileRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("Email already exists: " + newEmail);
        }

        UserProfile profile = findByUserId(userId);
        profile.setEmail(newEmail);

        UserProfile updated = userProfileRepository.save(profile);
        log.info("Email updated successfully for user ID: {}", userId);

        return updated;
    }

    /**
     * Deletes a user profile by user ID.
     *
     * @param userId the user ID to delete
     */
    @Transactional
    public void delete(Long userId) {
        log.info("Deleting user profile for user ID: {}", userId);

        if (!userProfileRepository.existsById(userId)) {
            log.warn("User profile not found for deletion: {}", userId);
            throw new IllegalArgumentException("User profile not found: " + userId);
        }

        userProfileRepository.deleteById(userId);
        log.info("User profile deleted successfully for user ID: {}", userId);
    }

    /**
     * Deletes a user profile by username.
     *
     * @param username the username to delete
     */
    @Transactional
    public void deleteByUsername(String username) {
        log.info("Deleting user profile for username: {}", username);

        UserProfile profile = findByUsername(username);
        userProfileRepository.delete(profile);
        log.info("User profile deleted successfully for username: {}", username);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Validates the create user profile request.
     *
     * @param request the request to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateCreateRequest(CreateUserProfileRequest request) {
        if (request.getUserId() == null || request.getUserId() <= 0) {
            throw new IllegalArgumentException("Invalid user ID: " + request.getUserId());
        }

        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be blank");
        }

        if (request.getFullname() == null || request.getFullname().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be blank");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be blank");
        }

        // Check for duplicates
        if (userProfileRepository.existsById(request.getUserId())) {
            throw new IllegalArgumentException("User profile already exists for ID: " + request.getUserId());
        }

        if (userProfileRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        if (userProfileRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }
    }
}
