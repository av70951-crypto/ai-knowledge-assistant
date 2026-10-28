package com.aryan.aiassistant.decision;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ChoiceDecisionService {

    public DecisionResult evaluate(
            String selectedOption,
            List<String> availableOptions,
            String explanation) {

        if (availableOptions == null || availableOptions.isEmpty()) {
            throw new IllegalArgumentException(
                    "Available options must not be empty.");
        }

        if (selectedOption == null ||
                !availableOptions.contains(selectedOption)) {
            throw new IllegalArgumentException(
                    "Selected option must be one of the available options.");
        }

        return new DecisionResult(
                "CHOICE",
                selectedOption,
                explanation
        );
    }
}