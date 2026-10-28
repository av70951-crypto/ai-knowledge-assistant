
package com.aryan.aiassistant.decision;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class DecisionEngine {

    private final DecisionTypeDetector decisionTypeDetector;
    private final DecisionService decisionService;

    public DecisionEngine(
            DecisionTypeDetector decisionTypeDetector,
            DecisionService decisionService) {
        this.decisionTypeDetector = decisionTypeDetector;
        this.decisionService = decisionService;
    }

    public DecisionResult evaluate(String question, List<String> evidence) {

        String type = decisionTypeDetector.detect(question);

        if (evidence == null || evidence.isEmpty()) {
            return new DecisionResult(
                    type,
                    "INSUFFICIENT_EVIDENCE",
                    "No relevant PDF evidence was found."
            );
        }

        String combinedEvidence = String.join(" ", evidence);
        String normalizedQuestion = question.toLowerCase(Locale.ROOT);
        String normalizedEvidence =
                combinedEvidence.toLowerCase(Locale.ROOT);

        if ("MULTI_CONDITION".equals(type)) {
            return evaluateMultiCondition(
                    normalizedQuestion,
                    normalizedEvidence
            );
        }

        if ("BOOLEAN".equals(type)) {
            return evaluateBoolean(
                    normalizedQuestion,
                    normalizedEvidence
            );
        }

        if ("CHOICE".equals(type)) {
            return evaluateChoice(
                    normalizedQuestion,
                    normalizedEvidence
            );
        }

        if ("SCORE".equals(type)) {
            return new DecisionResult(
                    "SCORE",
                    "REVIEW_REQUIRED",
                    "A reliable score cannot be calculated until "
                            + "explicit scoring criteria are defined."
            );
        }

        return new DecisionResult(
                "GENERAL",
                "EVIDENCE_FOUND",
                "Relevant document evidence was retrieved. "
                        + "See the answer and sources for details."
        );
    }

    private DecisionResult evaluateMultiCondition(
            String question,
            String evidence) {

        String[] conditions = question.split(
                "\\band\\b|\\bboth\\b|,"
        );

        List<String> meaningfulConditions = new ArrayList<>();

        for (String condition : conditions) {
            String cleaned = condition.trim();

            if (cleaned.length() > 3) {
                meaningfulConditions.add(cleaned);
            }
        }

        if (meaningfulConditions.size() < 2) {
            return new DecisionResult(
                    "MULTI_CONDITION",
                    "REVIEW_REQUIRED",
                    "The individual conditions could not be "
                            + "identified reliably."
            );
        }

        int supportedConditions = 0;

        for (String condition : meaningfulConditions) {
            if (isConditionSupported(condition, evidence)) {
                supportedConditions++;
            }
        }

        if (supportedConditions == meaningfulConditions.size()) {
            return new DecisionResult(
                    "MULTI_CONDITION",
                    "YES",
                    "All identified conditions are supported "
                            + "by the retrieved evidence."
            );
        }

        if (supportedConditions == 0) {
            return new DecisionResult(
                    "MULTI_CONDITION",
                    "REVIEW_REQUIRED",
                    "The retrieved evidence does not clearly "
                            + "support the requested conditions."
            );
        }

        return new DecisionResult(
                "MULTI_CONDITION",
                "REVIEW_REQUIRED",
                supportedConditions + " of "
                        + meaningfulConditions.size()
                        + " identified conditions were supported. "
                        + "All conditions could not be verified."
        );
    }

    private boolean isConditionSupported(
            String condition,
            String evidence) {

        String[] words = condition.toLowerCase(Locale.ROOT)
                .split("[^a-z0-9]+");

        int relevantWords = 0;
        int matchedWords = 0;

        for (String word : words) {
            if (word.length() < 4 || isStopWord(word)) {
                continue;
            }

            relevantWords++;

            if (evidence.contains(word)) {
                matchedWords++;
            }
        }

        if (relevantWords == 0) {
            return false;
        }

        // Require most meaningful terms to occur in the evidence.
        return matchedWords >= Math.ceil(relevantWords * 0.6);
    }

    private boolean isStopWord(String word) {
        return switch (word) {
            case "what", "when", "where", "which", "does",
                    "have", "has", "this", "that", "with",
                    "from", "into", "their", "there", "they",
                    "both", "must", "should", "would", "could",
                    "about", "also", "need", "required",
                    "requirements", "requirement" -> true;
            default -> false;
        };
    }

    private DecisionResult evaluateChoice(
            String question,
            String evidence) {

        List<String> options = new ArrayList<>();

        if (evidence.contains("embeddings")) {
            options.add("EMBEDDINGS");
        }

        if (evidence.contains("vector search")) {
            options.add("VECTOR_SEARCH");
        }

        if (evidence.contains("keyword search")) {
            options.add("KEYWORD_SEARCH");
        }

        if (options.isEmpty()) {
            return new DecisionResult(
                    "CHOICE",
                    "REVIEW_REQUIRED",
                    "No supported options were found in "
                            + "the retrieved evidence."
            );
        }

        String normalizedQuestion = question.toLowerCase(Locale.ROOT);

        List<String> questionMatches = new ArrayList<>();

        for (String option : options) {
            String searchableOption = option
                    .toLowerCase(Locale.ROOT)
                    .replace('_', ' ');

            if (normalizedQuestion.contains(searchableOption)) {
                questionMatches.add(option);
            }
        }

        if (questionMatches.size() == 1) {
            String selectedOption = questionMatches.get(0);

            return decisionService.evaluateChoice(
                    selectedOption,
                    options,
                    "The question specifically mentions "
                            + selectedOption
                            + ", which is supported by the retrieved evidence."
            );
        }

        if (options.size() == 1) {
            return decisionService.evaluateChoice(
                    options.get(0),
                    options,
                    "Only one supported option was identified "
                            + "in the retrieved evidence."
            );
        }

        return new DecisionResult(
                "CHOICE",
                "REVIEW_REQUIRED",
                "Multiple options were found in the evidence, "
                        + "but the question does not provide enough "
                        + "criteria to select one reliably. "
                        + "Available options: " + String.join(", ", options) + "."
        );
    }

    private DecisionResult evaluateBoolean(
            String question,
            String evidence) {

        boolean hasTenDocumentLimit =
                evidence.contains(
                        "maximum 10 pdf documents per organization"
                );

        boolean asksAboutDocumentCount =
                question.contains("pdf document")
                        || question.contains("pdf file");

        boolean asksIfMoreThanTenAllowed =
                question.contains("more than 10")
                        || question.contains("over 10")
                        || question.contains("exceed 10")
                        || question.contains("greater than 10");

        if (hasTenDocumentLimit
                && asksAboutDocumentCount
                && asksIfMoreThanTenAllowed) {

            return decisionService.evaluateBoolean(
                    false,
                    "NO. The PDF states that each organization "
                            + "can upload a maximum of 10 PDF documents."
            );
        }

        boolean asksAboutMaximum =
                question.contains("maximum")
                        || question.contains("limit")
                        || question.contains("up to 10");

        if (hasTenDocumentLimit
                && asksAboutDocumentCount
                && asksAboutMaximum) {

            return decisionService.evaluateBoolean(
                    true,
                    "YES. The PDF specifies a maximum of 10 "
                            + "PDF documents per organization."
            );
        }

        return new DecisionResult(
                "BOOLEAN",
                "REVIEW_REQUIRED",
                "The available evidence is not sufficient "
                        + "to make a reliable YES/NO decision."
        );
    }
}
