package com.vux38.auth.repository;

import com.vux38.auth.entity.RefreshToken;
import com.vux38.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // ========== ĐÃ SỬA: Tìm theo hash thay vì token gốc ==========

    /**
     * Find refresh token by token HASH (SHA-256)
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Delete refresh token by token HASH
     */
    @Modifying
    @Transactional
    void deleteByTokenHash(String tokenHash);

    // ========== GIỮ NGUYÊN: Các method khác vẫn dùng được ==========

    /**
     * Delete all refresh tokens for a specific user
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
    int deleteByUser(@Param("user") User user);

    /**
     * Delete all refresh tokens for a specific user ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * Delete all expired refresh tokens
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < CURRENT_TIMESTAMP")
    int deleteAllExpiredTokens();

    /**
     * Find latest refresh token for a user
     */
    Optional<RefreshToken> findTopByUserOrderByExpiryDateDesc(User user);

    /**
     * Find latest refresh token by user ID
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId ORDER BY rt.expiryDate DESC")
    Optional<RefreshToken> findTopByUserIdOrderByExpiryDateDesc(@Param("userId") Long userId);

    /**
     * Find all non-expired tokens (dùng cho cleanup hoặc kiểm tra)
     */
    List<RefreshToken> findByExpiryDateAfter(Instant now);
}