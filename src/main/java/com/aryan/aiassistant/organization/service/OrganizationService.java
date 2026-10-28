package com.aryan.aiassistant.organization.service;

import com.aryan.aiassistant.organization.entity.Organization;
import com.aryan.aiassistant.organization.repository.OrganizationRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    public OrganizationService(
            OrganizationRepository organizationRepository,
            PasswordEncoder passwordEncoder) {
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Organization registerOrganization(
            String name, String email, String password) {

        if (organizationRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "An organization with this email already exists.");
        }

        Organization organization = new Organization();

        organization.setName(name);
        organization.setEmail(email);
        organization.setPasswordHash(passwordEncoder.encode(password));
        organization.setPublicToken(generatePublicToken());
        organization.setCreatedAt(LocalDateTime.now());

        return organizationRepository.save(organization);
    }

    private String generatePublicToken() {
        byte[] tokenBytes = new byte[32];
        new SecureRandom().nextBytes(tokenBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);
    }
}