
package com.aryan.aiassistant.decision;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class DecisionTypeDetector {

    public String detect(String question) {

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Question must not be empty.");
        }

        String q = question.toLowerCase(Locale.ROOT).trim();

        // Detect questions requiring multiple conditions first.
        if (q.contains("both ")
                || q.contains("all conditions")
                || q.contains("multiple conditions")
                || q.contains(" and ")) {
            return "MULTI_CONDITION";
        }

        // Detect score or rating requests.
        if (q.contains("score")
                || q.contains("rate")
                || q.contains("rating")
                || q.contains("how many points")) {
            return "SCORE";
        }

        // Detect choice and comparison questions.
        if (q.contains("choose")
                || q.contains("choose between")
                || q.contains("which option")
                || q.contains("which category")
                || q.contains("which approach")
                || q.contains("which method")
                || q.contains("which retrieval")
                || q.contains("which one")
                || q.contains("select between")
                || q.contains("pick one")
                || q.contains("better option")
                || q.contains("best option")
                || q.contains("prefer")
                || q.contains("compare")
                || q.contains("versus")
                || q.contains(" vs ")
                || q.contains(" or ")) {
            return "CHOICE";
        }

        // Detect Boolean questions.
        if (q.startsWith("is ")
                || q.startsWith("are ")
                || q.startsWith("does ")
                || q.startsWith("do ")
                || q.startsWith("can ")
                || q.startsWith("has ")
                || q.startsWith("have ")
                || q.startsWith("will ")
                || q.startsWith("should ")
                || q.startsWith("must ")) {
            return "BOOLEAN";
        }

        // General information questions.
        if (q.startsWith("what ")
                || q.startsWith("why ")
                || q.startsWith("how ")
                || q.startsWith("when ")
                || q.startsWith("where ")
                || q.startsWith("who ")
                || q.startsWith("which ")) {
            return "GENERAL";
        }

        return "GENERAL";
    }
}
