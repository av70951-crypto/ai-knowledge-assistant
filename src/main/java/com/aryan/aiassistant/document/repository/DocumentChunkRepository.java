
package com.aryan.aiassistant.document.repository;

import com.aryan.aiassistant.document.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DocumentChunkRepository
        extends JpaRepository<DocumentChunk, Long> {

    List<DocumentChunk> findByOrganizationId(Long organizationId);

    List<DocumentChunk> findByDocumentIdAndOrganizationId(
            Long documentId, Long organizationId);

    @Modifying
    @Transactional
    @Query("""
            DELETE FROM DocumentChunk c
            WHERE c.documentId = :documentId
              AND c.organizationId = :organizationId
            """)
    void deleteByDocumentIdAndOrganizationId(
            @Param("documentId") Long documentId,
            @Param("organizationId") Long organizationId);
}
