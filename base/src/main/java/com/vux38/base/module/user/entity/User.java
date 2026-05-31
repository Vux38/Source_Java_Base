package com.vux38.base.module.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User entity represents application users.
 *
 * <p>
 * Supports authentication, authorization (multi-role),
 * and audit tracking.
 * </p>
 *
 * <b>Features:</b>
 * <ul>
 *     <li>Unique email for login</li>
 *     <li>Encrypted password storage</li>
 *     <li>Many-to-many relationship with Role</li>
 *     <li>Audit fields (created/updated)</li>
 *     <li>Soft activation (active flag)</li>
 * </ul>
 *
 * @author Vux38
 */
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_user_email", columnList = "email")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * Primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique email used for authentication
     */
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /**
     * Encrypted password (BCrypt)
     */
    @Column(nullable = false)
    private String password;

    /**
     * Display name
     */
    @Column(length = 100)
    private String name;

    /**
     * Account status (true = active)
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * User roles (RBAC)
     */
    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    /**
     * Created timestamp
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Updated timestamp
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Lifecycle hook: set createdAt before insert
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Lifecycle hook: set updatedAt before update
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}