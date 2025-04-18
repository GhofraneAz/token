package com.bookstore.springboot.entity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Utilisation de l'énumération ERole au lieu de String
    @Enumerated(EnumType.STRING) 
    @Column(length = 20, unique = true)
    private ERole name;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Permission> permissions = new HashSet<>();

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ERole getName() {
        return name;
    }

    public void setName(ERole name) {
        this.name = name;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }
}
