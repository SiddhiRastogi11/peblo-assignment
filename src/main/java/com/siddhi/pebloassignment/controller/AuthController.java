package com.siddhi.pebloassignment.controller;

import com.siddhi.pebloassignment.dto.AuthResponse;
import com.siddhi.pebloassignment.dto.LoginRequest;
import com.siddhi.pebloassignment.dto.SignupRequest;
import com.siddhi.pebloassignment.model.User;
import com.siddhi.pebloassignment.repository.UserRepository;
import com.siddhi.pebloassignment.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Allows your future React frontend to talk to this endpoint directly
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Error: Email is already registered!");
        }

        // Create a new user record with securely encrypted hashing
        User user = User.builder()
                .name(signupRequest.getName())
                .email(signupRequest.getEmail())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .build();

        userRepository.save(user);

        // Auto-login the user immediately after signing up by generating a token
        String token = jwtUtils.generateTokenFromEmail(user.getEmail());
        return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getName(), user.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Error: Invalid email or password credentials!");
        }

        // Generate token upon matching password validation
        String token = jwtUtils.generateTokenFromEmail(user.getEmail());
        return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getName(), user.getEmail()));
    }
}