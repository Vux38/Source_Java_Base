package com.vux38.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

/**
 * Core JWT token service for authentication and authorization.
 * <p>
 * This service handles the complete lifecycle of JWT tokens including:
 * <ul>
 *   <li>Token generation with unique IDs and expiration</li>
 *   <li>Token parsing and claim extraction</li>
 *   <li>Token validation and expiration checking</li>
 *   <li>Secure signing key generation using SHA-256</li>
 * </ul>
 * </p>
 * <p>
 * The service uses SHA-256 hashing of the secret key to ensure consistent
 * key length for HS256 algorithm regardless of input secret length.
 * </p>
 *
 * @author VUX38
 * @version 2.0
 * @since 2026
 */
@Slf4j
@Service
public class JwtTokenService {

    private final String secret;
    private final long expirationMs;

    /**
     * Constructs a new JwtTokenService with the specified configuration.
     *
     * @param secret the secret key used for signing tokens (will be hashed with SHA-256)
     * @param expirationMs the token expiration time in milliseconds
     */
    public JwtTokenService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs
    ) {
        this.secret = secret;
        this.expirationMs = expirationMs;
        log.info("JwtTokenService initialized with expiration: {} ms", expirationMs);
    }

    /**
     * Generates a new JWT token for the specified username.
     * <p>
     * The token includes:
     * <ul>
     *   <li>Unique JWT ID (JTI) - random UUID</li>
     *   <li>Subject - the username</li>
     *   <li>Issued at timestamp - current time</li>
     *   <li>Expiration timestamp - current time + configured expiration</li>
     * </ul>
     * </p>
     *
     * @param username the username to include as token subject
     * @return the generated JWT token string
     * @throws IllegalStateException if token generation fails
     */
    public String generateToken(String username) {
        log.debug("Generating JWT token for username: {}", username);

        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(expirationMs);
        String tokenId = UUID.randomUUID().toString();

        String token = Jwts.builder()
                .id(tokenId)
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey())
                .compact();

        log.debug("JWT token generated successfully with ID: {} for user: {}", tokenId, username);
        return token;
    }

    /**
     * Extracts the username (subject) from a JWT token.
     *
     * @param token the JWT token
     * @return the username from the token
     * @throws MalformedJwtException if token is malformed
     * @throws SecurityException if token signature is invalid
     * @throws ExpiredJwtException if token has expired
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the token ID (JTI) from a JWT token.
     *
     * @param token the JWT token
     * @return the unique token ID
     */
    public String extractTokenId(String token) {
        return extractClaim(token, Claims::getId);
    }

    /**
     * Extracts the issued date from a JWT token.
     *
     * @param token the JWT token
     * @return the issued date
     */
    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    /**
     * Extracts the expiration date from a JWT token.
     *
     * @param token the JWT token
     * @return the expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts the expiration timestamp in milliseconds from a JWT token.
     *
     * @param token the JWT token
     * @return the expiration timestamp in milliseconds, or null if extraction fails
     */
    public Long getExpirationTime(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration != null ? expiration.getTime() : null;
        } catch (Exception e) {
            log.warn("Failed to extract expiration time: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extracts a specific claim from a JWT token using a resolver function.
     *
     * @param token the JWT token
     * @param claimsResolver function to resolve the desired claim
     * @param <T> the type of the claim
     * @return the extracted claim value
     * @throws io.jsonwebtoken.JwtException if token parsing fails
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from a JWT token.
     *
     * @param token the JWT token
     * @return the Claims object containing all token data
     * @throws MalformedJwtException if token is malformed
     * @throws SecurityException if token signature is invalid
     * @throws ExpiredJwtException if token has expired
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Checks if a JWT token has expired.
     *
     * @param token the JWT token
     * @return true if token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            boolean expired = expiration != null && expiration.before(new Date());
            if (expired) {
                log.debug("Token has expired at: {}", expiration);
            }
            return expired;
        } catch (ExpiredJwtException e) {
            log.debug("Token is expired: {}", e.getMessage());
            return true;
        } catch (Exception e) {
            log.warn("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Validates a JWT token format and signature.
     * <p>
     * This method checks:
     * <ul>
     *   <li>Token format is valid JWT</li>
     *   <li>Signature is valid for the configured secret</li>
     *   <li>Token has not expired</li>
     * </ul>
     * </p>
     *
     * @param token the JWT token to validate
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            boolean isValid = !isTokenExpired(token);

            if (isValid) {
                log.debug("Token validation successful");
            } else {
                log.warn("Token validation failed: token expired");
            }

            return isValid;
        } catch (MalformedJwtException e) {
            log.warn("Token validation failed: malformed JWT - {}", e.getMessage());
            return false;
        } catch (SecurityException e) {
            log.warn("Token validation failed: invalid signature - {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validates a JWT token against a specific username.
     * <p>
     * Checks that the token username matches the expected username
     * and that the token is not expired.
     * </p>
     *
     * @param token the JWT token
     * @param username the expected username
     * @return true if token is valid and matches the username, false otherwise
     */
    public boolean validateToken(String token, String username) {
        try {
            String tokenUsername = extractUsername(token);
            boolean isValid = username.equals(tokenUsername) && !isTokenExpired(token);

            if (isValid) {
                log.debug("Token validated successfully for user: {}", username);
            } else {
                log.warn("Token validation failed for user: {}", username);
            }

            return isValid;
        } catch (Exception e) {
            log.warn("Token validation failed for user {}: {}", username, e.getMessage());
            return false;
        }
    }

    /**
     * Gets the remaining time until token expiration in milliseconds.
     *
     * @param token the JWT token
     * @return remaining time in milliseconds, or 0 if expired/error
     */
    public long getRemainingTime(String token) {
        try {
            Date expiration = extractExpiration(token);
            long remaining = expiration.getTime() - System.currentTimeMillis();
            return Math.max(0, remaining);
        } catch (Exception e) {
            log.warn("Failed to calculate remaining time: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Checks if a token can be refreshed (not expired for too long).
     * <p>
     * Tokens can be refreshed if they are expired but within the refresh window.
     * Default implementation allows refresh up to 24 hours after expiration.
     * </p>
     *
     * @param token the JWT token
     * @param maxRefreshWindowMs maximum window in milliseconds to allow refresh
     * @return true if token can be refreshed, false otherwise
     */
    public boolean isRefreshable(String token, long maxRefreshWindowMs) {
        try {
            Date expiration = extractExpiration(token);
            long expiredMs = System.currentTimeMillis() - expiration.getTime();
            boolean refreshable = expiredMs > 0 && expiredMs <= maxRefreshWindowMs;

            if (refreshable) {
                log.debug("Token is refreshable (expired {} ms ago)", expiredMs);
            }

            return refreshable;
        } catch (Exception e) {
            log.warn("Failed to check refreshability: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Creates a signing key from the configured secret using SHA-256 hashing.
     * <p>
     * This ensures the key is always 256 bits (32 bytes) regardless of the
     * input secret length, which is required for HS256 algorithm.
     * </p>
     *
     * @return the SecretKey for signing/verifying JWT tokens
     * @throws IllegalStateException if SHA-256 algorithm is not available
     */
    private SecretKey signingKey() {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(secret.getBytes(StandardCharsets.UTF_8));
            log.trace("Signing key generated using SHA-256");
            return Keys.hmacShaKeyFor(digest);
        } catch (NoSuchAlgorithmException ex) {
            log.error("SHA-256 algorithm not available", ex);
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}