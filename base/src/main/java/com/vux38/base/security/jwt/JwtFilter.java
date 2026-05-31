package com.vux38.base.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JWT Authentication Filter.
 *
 * <p>
 * Intercepts every HTTP request, extracts JWT from Authorization header,
 * validates token, and sets authentication into SecurityContext.
 * </p>
 *
 * <p>
 * This filter ensures stateless authentication using JWT and supports multi-role authorization.
 * </p>
 *
 * <b>Flow:</b>
 * <pre>
 * Request → JwtFilter → Validate Token → Set SecurityContext → Controller
 * </pre>
 *
 * <b>Security Notes:</b>
 * <ul>
 *     <li>Does NOT throw exception on invalid token (fail-safe design)</li>
 *     <li>Skips if Authorization header is missing</li>
 *     <li>Supports ROLE-based authorization</li>
 * </ul>
 *
 * @author Vux38
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    /**
     * Core filter logic executed once per request.
     *
     * @param request  HTTP request
     * @param response HTTP response
     * @param filterChain filter chain
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(AUTH_HEADER);

        // 1. Skip if header missing or not Bearer
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length());

        try {
            // 2. Validate token
            if (!jwtService.isValid(token)) {
                log.debug("Invalid JWT token");
                filterChain.doFilter(request, response);
                return;
            }

            // 3. Skip if already authenticated
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            // 4. Extract data from token
            String userId = jwtService.extractUserId(token);
            Set<String> roles = jwtService.extractRoles(token);

            // 5. Convert roles → authorities
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());

            // 6. Build authentication object
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            authorities
                    );

            // 7. Set authentication to SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception ex) {
            // Fail-safe: log and continue filter chain
            log.warn("JWT processing failed: {}", ex.getMessage());
        }

        // 8. Continue filter chain
        filterChain.doFilter(request, response);
    }
}