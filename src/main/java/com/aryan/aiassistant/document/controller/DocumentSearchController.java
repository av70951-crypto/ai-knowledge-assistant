
package com.aryan.aiassistant.document.controller;

import com.aryan.aiassistant.document.entity.DocumentChunk;
import com.aryan.aiassistant.document.service.DocumentSearchService;
import com.aryan.aiassistant.organization.entity.Organization;
import com.aryan.aiassistant.organization.repository.OrganizationRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentSearchController {

    private final DocumentSearchService documentSearchService;
    private final OrganizationRepository organizationRepository;

    public DocumentSearchController(
            DocumentSearchService documentSearchService,
            OrganizationRepository organizationRepository) {
        this.documentSearchService = documentSearchService;
        this.organizationRepository = organizationRepository;
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchDocuments(
            @RequestParam String question) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Authentication required."));
        }

        Organization organization = organizationRepository
                .findByEmail(authentication.getName())
                .orElse(null);

        if (organization == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Organization not found."));
        }

        try {
            List<DocumentChunk> results = documentSearchService.search(
                    organization.getId(), question);

            List<Map<String, Object>> matches = results.stream()
                    .map(chunk -> Map.<String, Object>of(
                            "documentId", chunk.getDocumentId(),
                            "pageNumber", chunk.getPageNumber(),
                            "content", chunk.getContent()))
                    .toList();

            return ResponseEntity.ok(Map.of(
                    "question", question,
                    "matchCount", matches.size(),
                    "matches", matches
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage()));
        }
    }
}
