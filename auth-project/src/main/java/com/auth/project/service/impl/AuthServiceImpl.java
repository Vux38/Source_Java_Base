package com.auth.project.service.impl;

import com.auth.project.dto.request.LoginRequest;
import com.auth.project.dto.request.RefreshTokenRequest;
import com.auth.project.dto.request.RegisterRequest;
import com.auth.project.dto.response.AuthResponse;
import com.auth.project.entity.RefreshToken;
import com.auth.project.entity.Role;
import com.auth.project.entity.User;
import com.auth.project.exception.BadRequestException;
import com.auth.project.exception.ResourceNotFoundException;
import com.auth.project.repository.RoleRepository;
import com.auth.project.repository.UserRepository;
import com.auth.project.security.UserDetailsImpl;
import com.auth.project.service.AuthService;
import com.auth.project.service.RefreshTokenService;
import com.auth.project.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String accessToken = jwtUtils.generateAccessToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        List<String> roles = userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

        log.info("User {} logged in successfully", userDetails.getUsername());

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken.getToken())
            .userId(userDetails.getId())
            .username(userDetails.getUsername())
            .email(userDetails.getEmail())
            .fullName(userDetails.getFullName())
            .roles(roles)
            .build();
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use: " + request.getEmail());
        }

        Set<Role> roles = resolveRoles(request.getRoles());

        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .fullName(request.getFullName())
            .roles(roles)
            .build();

        userRepository.save(user);
        log.info("New user registered: {}", user.getUsername());

        // Auto-login after register
        return login(new LoginRequest() {{
            setUsername(request.getUsername());
            setPassword(request.getPassword());
        }});
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken());
        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();
        String newAccessToken = jwtUtils.generateTokenFromUsername(user.getUsername());

        // Rotate refresh token
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

        List<String> roles = user.getRoles().stream()
            .map(r -> r.getName().name())
            .collect(Collectors.toList());

        return AuthResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken.getToken())
            .userId(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .roles(roles)
            .build();
    }

    @Override
    @Transactional
    public void logout(Long userId) {
        refreshTokenService.deleteByUserId(userId);
        SecurityContextHolder.clearContext();
        log.info("User {} logged out", userId);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────
    private Set<Role> resolveRoles(Set<String> requestedRoles) {
        Set<Role> roles = new HashSet<>();

        if (requestedRoles == null || requestedRoles.isEmpty()) {
            Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_USER"));
            roles.add(userRole);
        } else {
            requestedRoles.forEach(roleName -> {
                Role.RoleName roleEnum = switch (roleName.toLowerCase()) {
                    case "admin" -> Role.RoleName.ROLE_ADMIN;
                    case "moderator", "mod" -> Role.RoleName.ROLE_MODERATOR;
                    default -> Role.RoleName.ROLE_USER;
                };
                Role role = roleRepository.findByName(roleEnum)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleEnum));
                roles.add(role);
            });
        }
        return roles;
    }
}
