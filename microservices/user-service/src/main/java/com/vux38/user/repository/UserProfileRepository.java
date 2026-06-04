package com.vux38.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vux38.user.entity.UserProfile;

public interface UserProfileRepository
        extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByEmail(String email);

    boolean existsByEmail(String email);
}