package com.vux38.base.module.user.repository;

import com.vux38.base.module.user.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for User entity.
 *
 * <p>
 * Provides database operations for User.
 * </p>
 *
 * <b>Features:</b>
 * <ul>
 *     <li>Find by email (login)</li>
 *     <li>Check existence</li>
 *     <li>Pagination support</li>
 *     <li>Active user filtering</li>
 * </ul>
 *
 * @author Vux38
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find active users only
     */
    Page<User> findAllByActiveTrue(Pageable pageable);

    /**
     * Find active user by id
     */
    Optional<User> findByIdAndActiveTrue(Long id);
}