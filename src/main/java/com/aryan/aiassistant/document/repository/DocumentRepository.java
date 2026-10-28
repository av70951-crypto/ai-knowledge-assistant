
package com.aryan.aiassistant.document.repository;

import com.aryan.aiassistant.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByOrganizationId(Long organizationId);

    long countByOrganizationId(Long organizationId);

    Optional<Document> findByIdAndOrganizationId(
            Long id, Long organizationId);
}
