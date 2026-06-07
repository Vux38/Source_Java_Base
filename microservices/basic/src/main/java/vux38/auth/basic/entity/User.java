package vux38.auth.basic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String password;           // null nếu đăng nhập bằng Google

    @Column(nullable = false)
    private String fullName;

    @Column
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;     // LOCAL | GOOGLE

    @Column
    private String providerId;         // Google sub ID, null nếu LOCAL

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;                 // ROLE_USER | ROLE_ADMIN

    @Column(nullable = false)
    private boolean enabled;          // khoá tài khoản

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.enabled   = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}