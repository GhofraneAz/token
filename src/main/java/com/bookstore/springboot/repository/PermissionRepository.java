package com.bookstore.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookstore.springboot.entity.Permission;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);
}