package com.aryan.aiassistant.decision;

import org.springframework.stereotype.Service;

@Service
public class ScoreDecisionService {

    public DecisionResult evaluate(int score, String explanation) {

        if (score < 0 || score > 100) {
            throw new IllegalArgumentException(
                    "Score must be between 0 and 100.");
        }

        return new DecisionResult(
                "SCORE",
                String.valueOf(score),
                explanation
        );
    }
}