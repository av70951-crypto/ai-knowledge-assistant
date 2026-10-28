package com.aryan.aiassistant.decision;

import org.springframework.stereotype.Service;

@Service
public class BooleanDecisionService {

    public DecisionResult evaluate(boolean condition, String explanation) {

        String result = condition ? "YES" : "NO";

        return new DecisionResult(
                "BOOLEAN",
                result,
                explanation
        );
    }
}