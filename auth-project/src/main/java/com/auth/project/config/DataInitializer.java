package com.auth.project.config;

import com.auth.project.entity.Role;
import com.auth.project.entity.User;
import com.auth.project.repository.RoleRepository;
import com.auth.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            log.info("=== Seeding initial data ===");

            // 1. Seed Roles
            Arrays.stream(Role.RoleName.values()).forEach(roleName -> {
                if (roleRepository.findByName(roleName).isEmpty()) {
                    roleRepository.save(Role.builder().name(roleName).build());
                    log.info("Created role: {}", roleName);
                }
            });

            // 2. Seed Admin User
            if (!userRepository.existsByUsername("admin")) {
                Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN).orElseThrow();
                Role userRole  = roleRepository.findByName(Role.RoleName.ROLE_USER).orElseThrow();

                User admin = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .fullName("System Administrator")
                    .roles(Set.of(adminRole, userRole))
                    .build();

                userRepository.save(admin);
                log.info("Created default admin user (username: admin, password: Admin@123)");
            }

            // 3. Seed Moderator User
            if (!userRepository.existsByUsername("moderator")) {
                Role modRole  = roleRepository.findByName(Role.RoleName.ROLE_MODERATOR).orElseThrow();
                Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER).orElseThrow();

                User mod = User.builder()
                    .username("moderator")
                    .email("moderator@example.com")
                    .password(passwordEncoder.encode("Mod@123"))
                    .fullName("Content Moderator")
                    .roles(Set.of(modRole, userRole))
                    .build();

                userRepository.save(mod);
                log.info("Created default moderator user (username: moderator, password: Mod@123)");
            }

            // 4. Seed Regular User
            if (!userRepository.existsByUsername("user")) {
                Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER).orElseThrow();

                User user = User.builder()
                    .username("user")
                    .email("user@example.com")
                    .password(passwordEncoder.encode("User@123"))
                    .fullName("Regular User")
                    .roles(Set.of(userRole))
                    .build();

                userRepository.save(user);
                log.info("Created default regular user (username: user, password: User@123)");
            }

            log.info("=== Data seeding complete ===");
        };
    }
}
