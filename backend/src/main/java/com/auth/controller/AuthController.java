package com.auth.controller;

import com.auth.model.User;
import com.auth.security.JwtUtil;
import com.auth.service.AuthService;
import com.auth.service.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SessionManager sessionManager;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            authService.register(user);
            return ResponseEntity.ok().body(Map.of("message", "User registered successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        
        logger.debug("Login attempt - Username: {}, Password length: {}", username, password != null ? password.length() : "null");
        logger.debug("Raw credentials received: {}", credentials);

        Optional<User> userOpt = authService.authenticate(username, password);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password("")
                .authorities("USER")
                .build();

            String token = jwtUtil.generateToken(userDetails);
            sessionManager.createSession(username, token);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", username);
            response.put("userId", user.getId());
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.badRequest().body(Map.of("error", "Invalid credentials"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                String jwt = token.substring(7);
                String username = jwtUtil.extractUsername(jwt);
                
                // Even if token is expired, try to logout the user
                if (username != null) {
                    authService.logout(username);
                    return ResponseEntity.ok().body(Map.of("message", "Logged out successfully"));
                }
            }
            return ResponseEntity.ok().body(Map.of("message", "Logged out"));
        } catch (Exception e) {
            logger.error("Error during logout: {}", e.getMessage());
            // Still return OK to ensure client proceeds with logout
            return ResponseEntity.ok().body(Map.of("message", "Logged out"));
        }
    }
}
