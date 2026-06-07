package com.vux38.auth.config;

import com.vux38.auth.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for the Authentication Microservice.
 *
 * <p><b>Microservice Architecture Context:</b>
 * <br>This configuration is specific to AUTH-SERVICE and defines security rules
 * for authentication and authorization endpoints. It is completely independent
 * from other microservices' security configurations.
 * </p>
 *
 * <p><b>Security Features:</b>
 * <ul>
 *   <li>JWT-based stateless authentication</li>
 *   <li>Public endpoints for login, registration, and health checks</li>
 *   <li>Protected endpoints require valid JWT token</li>
 *   <li>Method-level security with {@code @PreAuthorize} annotations</li>
 *   <li>BCrypt password encoding for credential storage</li>
 *   <li>Session-less architecture (no server-side session state)</li>
 * </ul>
 * </p>
 *
 * <p><b>Public Endpoints:</b>
 * <ul>
 *   <li>{@code /api/auth/login} - User authentication</li>
 *   <li>{@code /api/auth/register} - New user registration</li>
 *   <li>{@code /api/auth/refresh} - Token refresh</li>
 *   <li>{@code /api/auth/health} - Service health check</li>
 *   <li>{@code /actuator/health} - Spring Boot actuator health</li>
 *   <li>{@code /actuator/info} - Service information</li>
 * </ul>
 * </p>
 *
 * @author VUX38
 * @version 2.0
 * @since 2026
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/api/auth/logout",
            "/api/auth/logout/**",
            "/api/auth/health",
            "/actuator/health",
            "/actuator/info"
    };

    private final JwtFilter jwtFilter;
    private final UserDetailsService userDetailsService;

    /**
     * Configures the main security filter chain for HTTP requests.
     *
     * @param http the {@link HttpSecurity} to configure
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.debug("Configuring SecurityFilterChain for auth-service");

        return http
                // Disable CSRF protection for stateless REST API
                .csrf(AbstractHttpConfigurer::disable)

                // Disable form-based authentication
                .formLogin(AbstractHttpConfigurer::disable)

                // Disable HTTP Basic authentication
                .httpBasic(AbstractHttpConfigurer::disable)

                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
                )

                // Use stateless session (no session creation or management)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Add JWT filter before Spring Security's default filter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                // Configure authentication provider
                .authenticationProvider(authenticationProvider())

                .build();
    }

    /**
     * Configures the authentication provider for username/password authentication.
     *
     * <p><b>Note for Spring Boot 4.x:</b> DaoAuthenticationProvider now requires
     * UserDetailsService in constructor. The setter method is deprecated.</p>
     *
     * @return the configured {@link AuthenticationProvider}
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        log.debug("Configuring DaoAuthenticationProvider");

        // Spring Boot 4.x: Constructor with UserDetailsService
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Exposes the authentication manager as a Spring bean.
     *
     * @param config the {@link AuthenticationConfiguration}
     * @return the {@link AuthenticationManager}
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        log.debug("Exposing AuthenticationManager bean");
        return config.getAuthenticationManager();
    }

    /**
     * Configures the password encoder for secure credential storage.
     *
     * @return the {@link PasswordEncoder} bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.debug("Configuring BCryptPasswordEncoder");
        return new BCryptPasswordEncoder();
    }
}