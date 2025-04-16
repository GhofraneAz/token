package com.bookstore.springboot.controller;

import java.util.HashSet;
import java.util.List;
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
    public ResponseEntity<Object> authenticateUser(@RequestBody AuthRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            return ResponseHandler.generateResponse("Authentication successful.", HttpStatus.OK,
                    new JwtResponse(jwt, null, userDetails.getUsername(), null, roles));

        } catch (BadCredentialsException e) {
            return ResponseHandler.generateResponse("Invalid username or password.", HttpStatus.UNAUTHORIZED, null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseHandler.generateResponse("Server error.", HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<Object> registerUser(@RequestBody User signUpRequest) {
        try {
            // Check if the username already exists
            if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                return ResponseHandler.generateResponse("Username is already in use", HttpStatus.UNPROCESSABLE_ENTITY, null);
            }

            // Encode the password
            signUpRequest.setPassword(encoder.encode(signUpRequest.getPassword()));

            // Activate user by default
            signUpRequest.setActived(true);

            // Assign roles passed in the request
            Set<Role> roles = new HashSet<>();
            for (Role role : signUpRequest.getRoles()) {
                // Retrieve role from the database
                java.util.Optional<Role> existingRoleOptional = roleRepository.findByName(role.getName());
                Role existingRole = existingRoleOptional.orElseGet(() -> {
                    // Optionally, create the role if it doesn't exist
                    roleRepository.save(role);
                    return role;
                });
                roles.add(existingRole);
            }
            signUpRequest.setRoles(roles);

            // Save the user to the database
            userRepository.save(signUpRequest);

            return ResponseHandler.generateResponse("User registered successfully", HttpStatus.CREATED, null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseHandler.generateResponse("Failed to sign up", HttpStatus.INTERNAL_SERVER_ERROR, null);
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
