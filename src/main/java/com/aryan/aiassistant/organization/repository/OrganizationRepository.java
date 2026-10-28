package com.aryan.aiassistant.organization.repository;

import com.aryan.aiassistant.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Optional<Organization> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Organization> findByPublicToken(String publicToken);
}