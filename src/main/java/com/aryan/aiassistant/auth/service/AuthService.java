package com.aryan.aiassistant.auth.service;

import com.aryan.aiassistant.auth.dto.LoginRequest;
import com.aryan.aiassistant.organization.entity.Organization;
import com.aryan.aiassistant.organization.repository.OrganizationRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            OrganizationRepository organizationRepository,
            PasswordEncoder passwordEncoder) {
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Organization login(LoginRequest request) {

        Organization organization = organizationRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid email or password."));

        if (!passwordEncoder.matches(
                request.getPassword(), organization.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        return organization;
    }
}