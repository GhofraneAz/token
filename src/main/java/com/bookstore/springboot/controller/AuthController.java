package com.bookstore.springboot.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.bookstore.springboot.dto.AuthRequest;
import com.bookstore.springboot.dto.SignupRequest;
import com.bookstore.springboot.entity.ERole;
import com.bookstore.springboot.entity.Role;
import com.bookstore.springboot.entity.User;
import com.bookstore.springboot.repository.RoleRepository;
import com.bookstore.springboot.repository.UserRepository;
import com.bookstore.springboot.response.JwtResponse;
import com.bookstore.springboot.response.ResponseHandler;
import com.bookstore.springboot.security.jwt.JwtUtils;
import com.bookstore.springboot.security.sevices.UserDetailsImpl;
import com.google.common.base.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    // @Autowired
    private Cache appUserCache;

    @PostMapping("/signin")
    public ResponseEntity<Map<String, String>> authenticateUser(@RequestBody User authenticationRequest) {
        try {
            // Authentifier l'utilisateur
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );
            
            // Enregistrer l'authentification dans le contexte de sécurité
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Générer le token JWT
            String token = jwtUtils.generateJwtToken(authentication);
            
            // Récupérer l'utilisateur authentifié
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            // Créer la réponse
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("type", "Bearer");
            response.put("username", userDetails.getUsername());
            response.put("email", userDetails.getEmail()); // Assurez-vous que l'email est inclus
            response.put("roles", userDetails.getAuthorities().toString()); // Assurez-vous que les rôles sont inclus
            
            // Retourner la réponse avec un statut 200 OK
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // En cas d'échec d'authentification
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Authentication failed");

            // Retourner la réponse d'erreur avec un statut 401 Unauthorized
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }


    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) {
        try {
            // 1) Vérifier si le username existe déjà
            if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                return ResponseHandler.generateResponse(
                    "Username is already in use",
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    null
                );
            }

            // 2) Construire l’entité User à partir du DTO
            User user = new User();
            user.setUsername(signUpRequest.getUsername());
            user.setEmail(signUpRequest.getEmail());
            user.setPassword(encoder.encode(signUpRequest.getPassword()));
            user.setActived(true);

            // 3) Création des rôles à partir des strings envoyées par le front
            Set<Role> roles = new HashSet<>();
            for (String roleName : signUpRequest.getRoles()) {
                try {
                    // Convertir le nom du rôle en ERole (Enum)
                    ERole enumRole = ERole.valueOf(roleName);

                    // Chercher le rôle dans la base de données
                    Role role = roleRepository.findByName(enumRole)
                        .orElseThrow(() -> new RuntimeException("Role not found in DB: " + enumRole));

                    // Ajouter le rôle à l'ensemble
                    roles.add(role);
                } catch (IllegalArgumentException ex) {
                    // Si le rôle est invalide ou non défini dans l'Enum
                    throw new RuntimeException("Invalid role: " + roleName);
                }
            }

            // 4) Assigner les rôles et sauvegarder l'utilisateur
            user.setRoles(roles);
            userRepository.save(user);

            return ResponseHandler.generateResponse(
                "User registered successfully",
                HttpStatus.CREATED,
                null
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseHandler.generateResponse(
                "Failed to sign up",
                HttpStatus.INTERNAL_SERVER_ERROR,
                null
            );
        }
    }


    @RequestMapping("/logout/{username}")
    public ResponseEntity<?> logout(@PathVariable("username") String pUsername,
                                    HttpServletRequest pRequest,
                                    HttpServletResponse pResponse) {
        // Tu peux ajouter ici la logique de suppression de token côté cache ou base si nécessaire

        return ResponseHandler.generateResponse("Logout not yet implemented for user: " + pUsername,
                HttpStatus.NOT_IMPLEMENTED, null);
    }
}
