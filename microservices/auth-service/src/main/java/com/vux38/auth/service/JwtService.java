package com.vux38.auth.service;

import com.vux38.auth.entity.User;
import com.vux38.auth.exception.AuthException;
import com.vux38.auth.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Service for JWT token operations.
 * <p>
 * This service acts as a facade for {@link JwtTokenService}, providing
 * convenience methods for token generation, validation, and extraction
 * specifically tailored for the application's User entity.
 * </p>
 *
 * @author VUX38
 * @version 1.0
 * @since 2024
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtTokenService jwtTokenService;

    /**
     * Generates an access token for the specified username.
     *
     * @param username the username to generate token for
     * @return the generated JWT token
     */
    public String generateAccessToken(String username) {
        log.debug("Generating access token for username: {}", username);
        String token = jwtTokenService.generateToken(username);
        log.debug("Access token generated successfully");
        return token;
    }

    /**
     * Generates a JWT token for a UserDetails object.
     * <p>
     * This method extracts the username from UserDetails and generates a token.
     * Use this method when you have a UserDetails object from Spring Security.
     * </p>
     *
     * @param userDetails the UserDetails object (typically User entity)
     * @return the generated JWT token
     */
    public String generateToken(UserDetails userDetails) {
        log.debug("Generating token for user: {}", userDetails.getUsername());
        String token = jwtTokenService.generateToken(userDetails.getUsername());
        log.debug("Token generated successfully for user: {}", userDetails.getUsername());
        return token;
    }

    /**
     * Generates a JWT token for a User entity.
     * <p>
     * This is a convenience method that delegates to {@link #generateToken(UserDetails)}.
     * </p>
     *
     * @param user the User entity
     * @return the generated JWT token
     */
    public String generateToken(User user) {
        return generateToken((UserDetails) user);
    }

    /**
     * Extracts the username (subject) from a JWT token.
     *
     * @param token the JWT token
     * @return the username extracted from the token
     * @throws AuthException if token extraction fails
     */
    public String extractUsername(String token) {
        try {
            log.debug("Extracting username from token");
            String username = jwtTokenService.extractUsername(token);
            log.debug("Username extracted: {}", username);
            return username;
        } catch (Exception e) {
            log.error("Failed to extract username from token", e);
            throw new AuthException("Invalid token format", e);
        }
    }

    /**
     * Validates a JWT token against UserDetails.
     * <p>
     * Checks that:
     * <ul>
     *   <li>The token username matches the UserDetails username</li>
     *   <li>The token is not expired</li>
     *   <li>The token signature is valid</li>
     * </ul>
     * </p>
     *
     * @param token the JWT token to validate
     * @param userDetails the UserDetails to validate against
     * @return true if token is valid, false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            log.debug("Validating token for user: {}", userDetails.getUsername());

            String username = extractUsername(token);
            boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);

            if (isValid) {
                log.debug("Token is valid for user: {}", userDetails.getUsername());
            } else {
                log.warn("Token is invalid for user: {}", userDetails.getUsername());
            }

            return isValid;
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validates a JWT token against a User entity.
     * <p>
     * Convenience method that delegates to {@link #isTokenValid(String, UserDetails)}.
     * </p>
     *
     * @param token the JWT token to validate
     * @param user the User entity to validate against
     * @return true if token is valid, false otherwise
     */
    public boolean isTokenValid(String token, User user) {
        return isTokenValid(token, (UserDetails) user);
    }

    /**
     * Checks if a JWT token has expired.
     *
     * @param token the JWT token to check
     * @return true if token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            boolean expired = jwtTokenService.isTokenExpired(token);
            log.debug("Token expired: {}", expired);
            return expired;
        } catch (Exception e) {
            log.warn("Failed to check token expiration: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Extracts the expiration time from a JWT token.
     *
     * @param token the JWT token
     * @return the expiration timestamp, or null if extraction fails
     */
    public Long getExpirationTime(String token) {
        try {
            return jwtTokenService.getExpirationTime(token);
        } catch (Exception e) {
            log.warn("Failed to extract expiration time: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Refreshes a token by generating a new one with extended expiration.
     * <p>
     * Note: This method does not validate the old token. Use {@link #isTokenValid(String, UserDetails)}
     * before calling this method.
     * </p>
     *
     * @param userDetails the UserDetails to generate new token for
     * @return the new JWT token
     */
    public String refreshToken(UserDetails userDetails) {
        log.info("Refreshing token for user: {}", userDetails.getUsername());
        String newToken = jwtTokenService.generateToken(userDetails.getUsername());
        log.debug("New token generated for user: {}", userDetails.getUsername());
        return newToken;
    }

    /**
     * Refreshes a token for a User entity.
     *
     * @param user the User entity
     * @return the new JWT token
     */
    public String refreshToken(User user) {
        return refreshToken((UserDetails) user);
    }
}