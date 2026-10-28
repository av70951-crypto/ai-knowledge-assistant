package com.aryan.aiassistant.decision;

public class DecisionResult {

    private String decisionType;
    private String result;
    private String explanation;

    public DecisionResult(String decisionType, String result, String explanation) {
        this.decisionType = decisionType;
        this.result = result;
        this.explanation = explanation;
    }

    public String getDecisionType() {
        return decisionType;
    }

    public String getResult() {
        return result;
    }

    public String getExplanation() {
        return explanation;
    }
}