package com.vux38.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing a refresh token for JWT authentication.
 *
 * <p><b>Microservice Architecture:</b>
 * <br>This entity belongs EXCLUSIVELY to AUTH-SERVICE database.
 * <br>USER-SERVICE does NOT have access to this table.
 * </p>
 *
 * <p><b>Security Features:</b>
 * <ul>
 *   <li>Tokens are UUID-based (cryptographically random)</li>
 *   <li>Only SHA-256 hash stored in database (plain text token NEVER persisted)</li>
 *   <li>Each token has an expiration date (default 7 days)</li>
 *   <li>Tokens are revocable via database deletion</li>
 *   <li>Relationship to User is many-to-one (one user can have multiple devices)</li>
 * </ul>
 * </p>
 *
 * @author VUX38
 * @version 3.0
 * @since 2026
 */
@Entity
@Table(name = "refresh_tokens",
        indexes = {
                @Index(name = "idx_token_hash", columnList = "tokenHash", unique = true),
                @Index(name = "idx_user_id", columnList = "user_id"),
                @Index(name = "idx_expiry_date", columnList = "expiry_date")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * SHA-256 hash of the token.
     * Plain text token is NEVER stored.
     * Length 64 = SHA-256 hex output.
     */
    @Column(name = "token_hash", unique = true, nullable = false, length = 64)
    private String tokenHash;

    /**
     * Transient field to temporarily hold raw token when returning to client.
     * NOT persisted in database.
     */
    @Transient
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Checks if this token has expired.
     *
     * @return true if token is expired, false otherwise
     */
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(Instant.now());
    }
}