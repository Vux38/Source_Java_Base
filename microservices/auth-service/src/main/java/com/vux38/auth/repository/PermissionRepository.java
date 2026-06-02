package com.vux38.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vux38.auth.entity.Permission;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);
}
