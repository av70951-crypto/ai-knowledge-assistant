package com.aryan.aiassistant.organization.controller;

import com.aryan.aiassistant.organization.entity.Organization;
import com.aryan.aiassistant.organization.service.OrganizationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerOrganization(
            @RequestBody Map<String, String> request) {

        try {
            String name = request.get("name");
            String email = request.get("email");
            String password = request.get("password");

            if (name == null || name.isBlank()
                    || email == null || email.isBlank()
                    || password == null || password.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error",
                                "Name, email, and password are required."));
            }

            Organization organization =
                    organizationService.registerOrganization(
                            name.trim(), email.trim(), password);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message", "Organization registered successfully.",
                            "organizationId", organization.getId(),
                            "name", organization.getName(),
                            "email", organization.getEmail(),
                            "publicToken", organization.getPublicToken(),
                            "assistantUrl", "http://localhost:8090/?token="
                                    + organization.getPublicToken(),
                            "embedCode", "<iframe src=\"http://localhost:8090/?token="
                                    + organization.getPublicToken()
                                    + "\" width=\"100%\" height=\"600\" "
                                    + "style=\"border:1px solid #ddd;border-radius:12px;\" "
                                    + "title=\"AI Knowledge Assistant\"></iframe>"
                    ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}