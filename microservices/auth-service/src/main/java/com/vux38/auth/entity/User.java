package com.vux38.auth.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Entity representing a user in the authentication system.
 * <p>
 * This entity implements {@link UserDetails} to integrate with Spring Security.
 * It stores authentication credentials and role/permission assignments.
 * </p>
 *
 * @author VUX38
 * @version 1.0
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "is_enabled")
    private boolean enabled = true;

    @Column(name = "is_account_non_locked")
    private boolean accountNonLocked = true;

    @Column(name = "is_account_non_expired")
    private boolean accountNonExpired = true;

    @Column(name = "is_credentials_non_expired")
    private boolean credentialsNonExpired = true;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> (GrantedAuthority) () -> permission.getName())
                .collect(Collectors.toSet());
    }

    /**
     * Builder pattern for User entity.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for User entity.
     */
    public static class Builder {
        private String username;
        private String password;
        private boolean enabled = true;
        private boolean accountNonLocked = true;
        private boolean accountNonExpired = true;
        private boolean credentialsNonExpired = true;
        private Set<Role> roles = new HashSet<>();

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder accountNonLocked(boolean accountNonLocked) {
            this.accountNonLocked = accountNonLocked;
            return this;
        }

        public Builder accountNonExpired(boolean accountNonExpired) {
            this.accountNonExpired = accountNonExpired;
            return this;
        }

        public Builder credentialsNonExpired(boolean credentialsNonExpired) {
            this.credentialsNonExpired = credentialsNonExpired;
            return this;
        }

        public Builder roles(Set<Role> roles) {
            this.roles = roles;
            return this;
        }

        public User build() {
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setEnabled(enabled);
            user.setAccountNonLocked(accountNonLocked);
            user.setAccountNonExpired(accountNonExpired);
            user.setCredentialsNonExpired(credentialsNonExpired);
            user.setRoles(roles);
            return user;
        }
    }
}