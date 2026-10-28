package com.aryan.aiassistant.auth.controller;

import com.aryan.aiassistant.auth.dto.LoginRequest;
import com.aryan.aiassistant.auth.service.AuthService;
import com.aryan.aiassistant.auth.service.JwtService;
import com.aryan.aiassistant.organization.entity.Organization;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(
            AuthService authService,
            JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        if (request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null
                || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email and password are required."));
        }

        try {
            Organization organization = authService.login(request);

            String token = jwtService.generateToken(
                    organization.getId(),
                    organization.getEmail());

            return ResponseEntity.ok(Map.of(
                    "message", "Login successful.",
                    "organizationId", organization.getId(),
                    "name", organization.getName(),
                    "email", organization.getEmail(),
                    "token", token
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}