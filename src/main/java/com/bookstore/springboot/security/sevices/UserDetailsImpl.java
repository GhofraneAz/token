package com.bookstore.springboot.security.sevices;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.bookstore.springboot.entity.Role;
import com.bookstore.springboot.entity.User;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class UserDetailsImpl implements UserDetails {

    private Long id;
    private String username;
    private String email;

    @JsonIgnore
    private String password;

    private boolean actived;

    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String username, String email, String password, boolean actived,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.actived = actived;
        this.authorities = authorities;
    }

 
        public static UserDetailsImpl build(User user) {
            // Récupérer les rôles de l'utilisateur, et transformer en liste de GrantedAuthority
            List<GrantedAuthority> authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.getName().name()))  // Récupérer le nom du rôle
                    .collect(Collectors.toList());

            // Assurer qu'il y a des rôles, sinon assigner un rôle par défaut
            if (authorities.isEmpty()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }

            // Retourner un UserDetailsImpl avec les valeurs correctes
            return new UserDetailsImpl(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail() != null ? user.getEmail() : "",  // Assurer que l'email n'est pas nul
                    user.getPassword(),
                    user.getActived(),  // Vérifier que la méthode pour l'activation est correcte
                    authorities);
        }


    // Méthodes requises par l'interface UserDetails

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return actived; // Compte bloqué si non activé
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return actived;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
}
