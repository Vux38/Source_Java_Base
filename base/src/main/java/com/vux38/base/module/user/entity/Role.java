package com.vux38.base.module.user.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Role entity for RBAC system.
 *
 * Example:
 * USER, ADMIN
 *
 * @author Vux38
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Role name (e.g. USER, ADMIN)
     */
    @Column(nullable = false, unique = true)
    private String name;

    private String description;
}