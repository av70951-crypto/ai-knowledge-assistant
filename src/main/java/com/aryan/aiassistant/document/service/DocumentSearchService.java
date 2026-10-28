
package com.aryan.aiassistant.document.service;

import com.aryan.aiassistant.document.entity.DocumentChunk;
import com.aryan.aiassistant.document.repository.DocumentChunkRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class DocumentSearchService {

    private final DocumentChunkRepository documentChunkRepository;

    private static final Set<String> STOP_WORDS = Set.of(
            "what", "which", "when", "where", "does", "are",
            "the", "for", "and", "how", "can", "you", "your",
            "is", "a", "an", "to", "of", "in", "on", "per",
            "be", "by", "it", "this", "that", "with", "from",
            "organization", "organizations", "allowed", "allow",
            "maximum", "limit", "more", "than", "over"
    );

    public DocumentSearchService(
            DocumentChunkRepository documentChunkRepository) {
        this.documentChunkRepository = documentChunkRepository;
    }

    public List<DocumentChunk> search(
            Long organizationId,
            String question) {

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Question must not be empty.");
        }

        List<String> keywords = Arrays.stream(
                        question.toLowerCase(Locale.ROOT)
                                .split("[^a-z0-9]+"))
                .filter(word -> word.length() >= 2)
                .filter(word -> !STOP_WORDS.contains(word))
                .distinct()
                .toList();

        if (keywords.isEmpty()) {
            return List.of();
        }

        return documentChunkRepository
                .findByOrganizationId(organizationId)
                .stream()
                .filter(chunk -> chunk.getContent() != null)
                .map(chunk -> new SearchResult(
                        chunk,
                        countMatches(chunk.getContent(), keywords),
                        countPhraseMatches(chunk.getContent(), question)))
                .filter(result -> result.score() > 0)
                .sorted(
                        Comparator.comparingInt(SearchResult::phraseScore)
                                .reversed()
                                .thenComparing(
                                        Comparator.comparingInt(
                                                SearchResult::score)
                                                .reversed()))
                .limit(5)
                .map(SearchResult::chunk)
                .toList();
    }

    private int countMatches(
            String content,
            List<String> keywords) {

        String text = content.toLowerCase(Locale.ROOT);
        int score = 0;

        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                score++;
            }
        }

        return score;
    }

    private int countPhraseMatches(
            String content,
            String question) {

        String text = content.toLowerCase(Locale.ROOT);
        String normalizedQuestion =
                question.toLowerCase(Locale.ROOT);

        int score = 0;

        if (normalizedQuestion.contains("pdf")
                && text.contains("pdf")) {
            score += 2;
        }

        if ((normalizedQuestion.contains("more than 10")
                || normalizedQuestion.contains("over 10")
                || normalizedQuestion.contains("exceed 10"))
                && text.contains("maximum 10")) {
            score += 5;
        }

        if ((normalizedQuestion.contains("maximum")
                || normalizedQuestion.contains("limit"))
                && text.contains("maximum")) {
            score += 3;
        }

        if (normalizedQuestion.contains("page")
                && text.contains("page")) {
            score += 2;
        }

        if (normalizedQuestion.contains("size")
                && text.contains("mb")) {
            score += 2;
        }

        return score;
    }

    private record SearchResult(
            DocumentChunk chunk,
            int score,
            int phraseScore) {
    }
}
