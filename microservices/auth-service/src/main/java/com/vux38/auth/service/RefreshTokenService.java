package com.vux38.auth.service;

import com.vux38.auth.dto.response.AuthResponse;
import com.vux38.auth.entity.RefreshToken;
import com.vux38.auth.entity.User;
import com.vux38.auth.exception.TokenException;
import com.vux38.auth.repository.RefreshTokenRepository;
import com.vux38.auth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
public class RefreshTokenService {

    // ==================== CONSTANTS ====================

    private static final long REFRESH_TOKEN_DURATION_DAYS = 7L;
    private static final long REFRESH_TOKEN_DURATION_SECONDS = REFRESH_TOKEN_DURATION_DAYS * 24 * 60 * 60;
    private static final int MIN_TOKEN_LENGTH_FOR_MASKING = 8;
    private static final int TOKEN_MASK_PREFIX_LENGTH = 6;
    private static final int TOKEN_MASK_SUFFIX_LENGTH = 4;
    private static final String TOKEN_MASK_PLACEHOLDER = "***";

    // ==================== DEPENDENCIES ====================

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            JwtService jwtService
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    // ==================== HASHING UTILITIES ====================

    /**
     * Hash token using SHA-256 (hex encoded).
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    // ==================== PUBLIC API - TOKEN CREATION ====================

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        validateUserNotNull(user);
        log.debug("Creating refresh token for user: {}", user.getUsername());

        String rawToken = generateSecureToken();
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(tokenHash)            // Lưu hash
                .user(user)
                .expiryDate(calculateExpiryDate())
                .build();

        refreshTokenRepository.save(refreshToken);
        log.info("Refresh token created for user: {}", user.getUsername());

        // Trả token gốc cho client (qua @Transient field)
        refreshToken.setToken(rawToken);
        return refreshToken;
    }

    @Transactional
    public RefreshToken create(User user) {
        return createRefreshToken(user);
    }

    // ==================== PUBLIC API - TOKEN VERIFICATION ====================

    public RefreshToken verify(String rawToken) {
        validateTokenString(rawToken);
        log.debug("Verifying refresh token: {}", maskToken(rawToken));

        String tokenHash = hashToken(rawToken);
        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> {
                    log.warn("Refresh token not found: {}", maskToken(rawToken));
                    return new TokenException("Invalid refresh token");
                });

        validateTokenNotExpired(refreshToken);

        log.debug("Refresh token verified for user: {}",
                refreshToken.getUser().getUsername());
        return refreshToken;
    }

    public boolean isValid(String token) {
        try {
            verify(token);
            return true;
        } catch (TokenException e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // ==================== PUBLIC API - LOGOUT OPERATIONS ====================

    @Transactional
    public void logout(String rawToken) {
        if (!isValidTokenString(rawToken)) {
            log.warn("Logout called with null or empty refresh token");
            return;
        }

        log.info("Processing logout for token: {}", maskToken(rawToken));

        String tokenHash = hashToken(rawToken);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            refreshTokenRepository.delete(token);
            log.info("Refresh token invalidated for user: {}",
                    token.getUser().getUsername());
        });
    }

    @Transactional
    public int logoutByUserId(Long userId) {
        validateUserId(userId);
        log.info("Invalidating all sessions for user id: {}", userId);

        User user = findUserOrThrow(userId);
        int deletedCount = refreshTokenRepository.deleteByUser(user);

        log.info("Invalidated {} refresh tokens for user: {}", deletedCount, user.getUsername());
        return deletedCount;
    }

    @Transactional
    public int logoutAllDevices(String username) {
        if (!isValidUsername(username)) {
            log.warn("Invalid username provided for logout all devices");
            return 0;
        }

        log.info("Invalidating all devices for user: {}", username);

        return userRepository.findByUsername(username)
                .map(user -> {
                    int deletedCount = refreshTokenRepository.deleteByUser(user);
                    log.info("Invalidated {} refresh tokens for user: {}", deletedCount, username);
                    return deletedCount;
                })
                .orElseGet(() -> {
                    log.warn("User not found for logout all devices: {}", username);
                    return 0;
                });
    }

    // ==================== PUBLIC API - TOKEN REFRESH ====================

    public AuthResponse refreshAccessToken(String rawToken) {
        log.info("Processing access token refresh request");

        RefreshToken verified = verify(rawToken);
        User user = verified.getUser();

        String newAccessToken = jwtService.generateToken(user);

        log.info("New access token issued for user: {}", user.getUsername());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(rawToken)      // Trả lại token gốc
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }

    public String findRefreshTokenByAccessToken(String accessToken) {
        if (!isValidTokenString(accessToken)) {
            log.warn("Null or empty access token provided");
            return null;
        }

        try {
            String username = jwtService.extractUsername(accessToken);
            return userRepository.findByUsername(username)
                    .flatMap(refreshTokenRepository::findTopByUserOrderByExpiryDateDesc)
                    .map(RefreshToken::getToken)   // Trả về @Transient token gốc (có thể null nếu load từ DB)
                    .orElse(null);
        } catch (Exception e) {
            log.warn("Failed to extract refresh token from access token: {}", e.getMessage());
            return null;
        }
    }

    // ==================== PUBLIC API - TOKEN MANAGEMENT ====================

    @Transactional
    public void deleteByToken(String rawToken) {
        if (!isValidTokenString(rawToken)) {
            log.warn("Cannot delete null or empty token");
            return;
        }

        log.debug("Deleting refresh token: {}", maskToken(rawToken));
        String tokenHash = hashToken(rawToken);
        refreshTokenRepository.deleteByTokenHash(tokenHash);
    }

    @Transactional
    public int cleanupExpiredTokens() {
        log.info("Starting cleanup of expired refresh tokens");
        int deletedCount = refreshTokenRepository.deleteAllExpiredTokens();
        log.info("Cleaned up {} expired refresh tokens", deletedCount);
        return deletedCount;
    }

    public User getUserByToken(String token) {
        RefreshToken refreshToken = verify(token);
        return refreshToken.getUser();
    }

    // ==================== PRIVATE HELPERS ====================

    private String generateSecureToken() {
        return UUID.randomUUID().toString();
    }

    private Instant calculateExpiryDate() {
        return Instant.now().plusSeconds(REFRESH_TOKEN_DURATION_SECONDS);
    }

    private void validateUserNotNull(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null when creating refresh token");
        }
    }

    private void validateTokenString(String token) {
        if (!isValidTokenString(token)) {
            throw new TokenException("Refresh token cannot be null or empty");
        }
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new TokenException("Invalid user ID: " + userId);
        }
    }

    private boolean isValidUsername(String username) {
        return username != null && !username.trim().isEmpty();
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for logout: {}", userId);
                    return new TokenException("User not found: " + userId);
                });
    }

    private void validateTokenNotExpired(RefreshToken refreshToken) {
        if (refreshToken.isExpired()) {
            log.warn("Refresh token expired for user: {}",
                    refreshToken.getUser().getUsername());
            throw new TokenException("Refresh token has expired");
        }
    }

    private boolean isValidTokenString(String token) {
        return token != null && !token.trim().isEmpty();
    }

    private String maskToken(String token) {
        if (token == null || token.length() <= MIN_TOKEN_LENGTH_FOR_MASKING) {
            return TOKEN_MASK_PLACEHOLDER;
        }
        return token.substring(0, TOKEN_MASK_PREFIX_LENGTH) +
                "..." +
                token.substring(token.length() - TOKEN_MASK_SUFFIX_LENGTH);
    }
}