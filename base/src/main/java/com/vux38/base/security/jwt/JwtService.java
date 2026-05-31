package com.vux38.base.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

/**
 * JWT Service.
 *
 * <p>
 * Handles JWT generation, validation and extraction.
 * Supports Access & Refresh tokens (production-ready).
 * </p>
 *
 * @author Vux38
 */
@Slf4j
@Service
public class JwtService {

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    @Value("${JWT_EXPIRATION}")
    private long jwtExpiration;

    @Value("${JWT_REFRESH_EXPIRATION}")
    private long jwtRefreshExpiration;

    // ================= GENERATE =================

    /**
     * Generate Access Token
     */
    public String generateAccessToken(String userId,
                                      String email,
                                      Set<String> roles) {

        return Jwts.builder()
                .id(UUID.randomUUID().toString()) // jti
                .subject(userId)
                .claim("roles", roles)
                .claim("email", email)
                .claim("type", "ACCESS")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignKey())
                .compact();
    }

    /**
     * Generate Refresh Token
     */
    public String generateRefreshToken(String userId) {

        return Jwts.builder()
                .id(UUID.randomUUID().toString()) // jti
                .subject(userId)
                .claim("type", "REFRESH")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtRefreshExpiration))
                .signWith(getSignKey())
                .compact();
    }

    // ================= VALIDATE =================

    public boolean isValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !isExpired(claims);
        } catch (Exception e) {
            log.debug("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    public boolean isExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    // ================= EXTRACT =================

    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractType(String token) {
        return extractClaim(token, claims -> (String) claims.get("type"));
    }

    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    @SuppressWarnings("unchecked")
    public Set<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return new HashSet<>((List<String>) claims.get("roles"));
    }

    // ================= INTERNAL =================

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Optional helper (có thể dùng hoặc bỏ)
     */
    private long getRefreshExpiration() {
        return jwtRefreshExpiration;
    }
}