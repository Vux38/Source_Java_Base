package com.vux38.user.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "user_profiles",
        indexes = {
                @Index(name = "idx_profile_email", columnList = "email")
        }
)
public class UserProfile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "avatar_url", length = 500)
    private String avatar;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}