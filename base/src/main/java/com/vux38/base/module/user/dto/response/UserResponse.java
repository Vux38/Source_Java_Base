package com.vux38.base.module.user.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Response DTO for user data.
 *
 * <p>
 * Used to return safe user information to clients.
 * </p>
 *
 * <b>Excludes sensitive fields:</b>
 * <ul>
 *     <li>Password</li>
 * </ul>
 *
 * @author Vux38
 */
@Data
@Builder
public class UserResponse {

    private Long id;

    private String email;

    private String name;

    private boolean active;

    /**
     * User roles (e.g. USER, ADMIN)
     */
    private Set<String> roles;

    /**
     * Audit fields
     */
    private LocalDateTime createdAt;
}