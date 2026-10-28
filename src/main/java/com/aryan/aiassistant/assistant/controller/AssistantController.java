
package com.aryan.aiassistant.assistant.controller;

import com.aryan.aiassistant.assistant.service.GeminiService;
import com.aryan.aiassistant.document.entity.DocumentChunk;
import com.aryan.aiassistant.document.service.DocumentSearchService;
import com.aryan.aiassistant.organization.entity.Organization;
import com.aryan.aiassistant.organization.repository.OrganizationRepository;
import com.aryan.aiassistant.document.repository.DocumentRepository;
import com.aryan.aiassistant.decision.DecisionEngine;
import com.aryan.aiassistant.decision.DecisionResult;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final OrganizationRepository organizationRepository;
    private final DocumentSearchService documentSearchService;
    private final DocumentRepository documentRepository;
    private final DecisionEngine decisionEngine;
    private final GeminiService geminiService;

    public AssistantController(
            OrganizationRepository organizationRepository,
            DocumentSearchService documentSearchService,
            DocumentRepository documentRepository,
            DecisionEngine decisionEngine,
            GeminiService geminiService) {

        this.organizationRepository = organizationRepository;
        this.documentSearchService = documentSearchService;
        this.documentRepository = documentRepository;
        this.decisionEngine = decisionEngine;
        this.geminiService = geminiService;
    }

    @PostMapping("/ask")
    public ResponseEntity<?> askQuestion(
            @RequestBody Map<String, String> request) {

        String assistantToken = request.get("assistantToken");
        String question = request.get("question");

        if (assistantToken == null || assistantToken.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Assistant token is required."));
        }

        if (question == null || question.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Question must not be empty."));
        }

        Organization organization = organizationRepository
                .findByPublicToken(assistantToken)
                .orElse(null);

        if (organization == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "Invalid assistant token."));
        }

        try {
            // Search only documents belonging to this organization.
            List<DocumentChunk> results = documentSearchService.search(
                    organization.getId(), question);

            // Build source citations after verifying document ownership.
            List<Map<String, Object>> allSources = results.stream()
                    .map(chunk -> {
                        var document = documentRepository
                                .findByIdAndOrganizationId(
                                        chunk.getDocumentId(),
                                        organization.getId())
                                .orElse(null);

                        // Never expose a citation for an unverified document.
                        if (document == null
                                || chunk.getContent() == null
                                || chunk.getContent().isBlank()) {
                            return null;
                        }

                        String fileName = document.getFileName();

                        Map<String, Object> source = new LinkedHashMap<>();
                        source.put("documentId", chunk.getDocumentId());
                        source.put("fileName", fileName);
                        source.put("pageNumber", chunk.getPageNumber());
                        source.put(
                                "reference",
                                fileName + " - Page " + chunk.getPageNumber());
                        source.put("content", chunk.getContent());

                        return source;
                    })
                    .filter(Objects::nonNull)
                    .toList();

            // Remove duplicate citations while preserving search order.
            Map<String, Map<String, Object>> uniqueSources =
                    new LinkedHashMap<>();

            for (Map<String, Object> source : allSources) {
                String key = source.get("fileName") + "|"
                        + source.get("pageNumber") + "|"
                        + source.get("content");

                uniqueSources.putIfAbsent(key, source);
            }

            List<Map<String, Object>> sources =
                    List.copyOf(uniqueSources.values());

            List<String> evidence = sources.stream()
                    .map(source -> (String) source.get("content"))
                    .filter(content -> !content.isBlank())
                    .toList();

            // If no verified evidence exists, do not call Gemini.
            if (evidence.isEmpty()) {
                DecisionResult decision = decisionEngine.evaluate(
                        question, evidence);

                return ResponseEntity.ok(Map.of(
                        "answer",
                        "I couldn't find relevant information in this organization's knowledge base.",
                        "sources", sources,
                        "status", "INSUFFICIENT_EVIDENCE",
                        "decision", Map.of(
                                "decisionType", decision.getDecisionType(),
                                "result", "INSUFFICIENT_EVIDENCE",
                                "explanation",
                                "No relevant evidence was retrieved from this organization's documents."
                        )
                ));
            }

            String evidenceText = sources.stream()
                    .map(source ->
                            "Source: " + source.get("reference")
                                    + "\nContent: " + source.get("content"))
                    .collect(Collectors.joining("\n\n"));

            String answer = geminiService.generateAnswer(
                    question, evidenceText);

            String normalizedAnswer = answer == null
                    ? ""
                    : answer.toLowerCase(Locale.ROOT);

            boolean insufficientEvidence =
                    normalizedAnswer.contains(
                            "could not find sufficient information")
                    || normalizedAnswer.contains(
                            "couldn't find sufficient information")
                    || normalizedAnswer.contains(
                            "could not find relevant information")
                    || normalizedAnswer.contains(
                            "couldn't find relevant information")
                    || normalizedAnswer.contains(
                            "insufficient information in the uploaded documents");

            DecisionResult decision = decisionEngine.evaluate(
                    question, evidence);

            if (insufficientEvidence) {
                return ResponseEntity.ok(Map.of(
                        "answer", answer,
                        "sources", sources,
                        "status", "INSUFFICIENT_EVIDENCE",
                        "decision", Map.of(
                                "decisionType", decision.getDecisionType(),
                                "result", "INSUFFICIENT_EVIDENCE",
                                "explanation",
                                "The generated answer indicates that the available evidence does not answer the question."
                        )
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "answer", answer,
                    "sources", sources,
                    "status", "ANSWERED",
                    "decision", Map.of(
                            "decisionType", decision.getDecisionType(),
                            "result", decision.getResult(),
                            "explanation", decision.getExplanation()
                    )
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(
                    HttpStatus.SERVICE_UNAVAILABLE).body(
                    Map.of(
                            "message",
                            "The assistant is temporarily unable to generate an answer. Please try again."
                    ));
        }
    }
}
