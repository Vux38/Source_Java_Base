package com.vux38.base.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import jakarta.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.flywaydb.core.internal.parser.TokenType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/**
 * JWT Provider.
 *
 * <p>
 * Handles low-level JWT operations:
 * generate, parse, validate token.
 * </p>
 *
 * <b>Responsibilities:</b>
 * <ul>
 *     <li>Generate JWT tokens</li>
 *     <li>Parse claims</li>
 *     <li>Validate signature & expiration</li>
 * </ul>
 *
 * <b>Design:</b>
 * <ul>
 *     <li>Stateless</li>
 *     <li>Supports ACCESS & REFRESH token</li>
 *     <li>Uses JJWT 0.12+ API</li>
 * </ul>
 *
 * @author Vux38
 */
@Slf4j
@Component
public class JwtProvider {

    private Key key;

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    /**
     * Initialize signing key
     */
    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate JWT token
     *
     * @param userId user identifier
     * @param roles user roles
     * @param type token type (ACCESS / REFRESH)
     * @param expiration expiration time (ms)
     * @return JWT string
     */
    public String generateToken(String userId,
                                Set<String> roles,
                                TokenType type,
                                long expiration) {

        return Jwts.builder()
                .subject(userId)
                .claim("roles", roles)
                .claim("type", type.name())
                .id(UUID.randomUUID().toString()) // jti
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    /**
     * Parse JWT claims
     *
     * @param token JWT
     * @return Claims
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Validate token (signature + expiration)
     *
     * @param token JWT
     * @return true if valid
     */
    public boolean isValid(String token) {
        try {
            Claims claims = parse(token);
            return !isExpired(claims);
        } catch (Exception e) {
            log.debug("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check expiration
     */
    public boolean isExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    /**
     * Extract userId
     */
    public String getUserId(Claims claims) {
        return claims.getSubject();
    }

    /**
     * Extract roles
     */
    @SuppressWarnings("unchecked")
    public Set<String> getRoles(Claims claims) {
        return Set.copyOf((java.util.List<String>) claims.get("roles"));
    }

    /**
     * Extract token type
     */
    public TokenType getTokenType(Claims claims) {
        return TokenType.valueOf((String) claims.get("type"));
    }

    /**
     * Extract token id (jti)
     */
    public String getTokenId(Claims claims) {
        return claims.getId();
    }
}