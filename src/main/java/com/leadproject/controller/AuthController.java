package com.leadproject.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    @PostMapping("/auth/login")
    public ResponseEntity<Map<String, Object>> login(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", "Authentication required. Use Basic Auth with a valid username and password."
            ));
        }

        return ResponseEntity.ok(Map.of(
                "user", authentication.getName(),
                "roles", authentication.getAuthorities().stream().map(Object::toString).toList(),
                "message", "Authentication successful"
        ));
    }
}
