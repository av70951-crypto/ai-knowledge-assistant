
package com.aryan.aiassistant.document.controller;

import com.aryan.aiassistant.document.entity.Document;
import com.aryan.aiassistant.document.service.DocumentService;
import com.aryan.aiassistant.organization.entity.Organization;
import com.aryan.aiassistant.organization.repository.OrganizationRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final OrganizationRepository organizationRepository;

    public DocumentController(
            DocumentService documentService,
            OrganizationRepository organizationRepository) {
        this.documentService = documentService;
        this.organizationRepository = organizationRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadPdf(
            @RequestParam("file") MultipartFile file) {

        try {
            Organization organization = getAuthenticatedOrganization();

            Document document = documentService.uploadPdf(
                    file, organization.getId());

            return ResponseEntity.ok(Map.of(
                    "message", "PDF uploaded successfully.",
                    "documentId", document.getId(),
                    "fileName", document.getFileName(),
                    "pageCount", document.getPageCount(),
                    "organizationId", document.getOrganizationId()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (AuthenticationRequiredException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @DeleteMapping("/{documentId}")
    public ResponseEntity<?> deleteDocument(
            @PathVariable Long documentId) {

        try {
            Organization organization = getAuthenticatedOrganization();

            documentService.deleteDocument(
                    documentId, organization.getId());

            return ResponseEntity.ok(Map.of(
                    "message", "Document deleted successfully.",
                    "documentId", documentId
            ));

        } catch (AuthenticationRequiredException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getDocuments() {
        try {
            Organization organization = getAuthenticatedOrganization();

            List<Document> documents =
                    documentService.getDocuments(organization.getId());

            List<Map<String, Object>> result = documents.stream()
                    .map(document -> Map.<String, Object>of(
                            "documentId", document.getId(),
                            "fileName", document.getFileName(),
                            "pageCount", document.getPageCount(),
                            "uploadedAt", document.getUploadedAt()
                    ))
                    .toList();

            return ResponseEntity.ok(result);

        } catch (AuthenticationRequiredException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    private Organization getAuthenticatedOrganization() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            throw new AuthenticationRequiredException(
                    "Authentication required.");
        }

        return organizationRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Organization not found. Please log in again."));
    }

    private static class AuthenticationRequiredException
            extends RuntimeException {
        private AuthenticationRequiredException(String message) {
            super(message);
        }
    }
}
