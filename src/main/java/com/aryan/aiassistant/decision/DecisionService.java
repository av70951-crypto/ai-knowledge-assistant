package com.aryan.aiassistant.decision;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DecisionService {

    private final BooleanDecisionService booleanDecisionService;
    private final ChoiceDecisionService choiceDecisionService;
    private final ScoreDecisionService scoreDecisionService;

    public DecisionService(
            BooleanDecisionService booleanDecisionService,
            ChoiceDecisionService choiceDecisionService,
            ScoreDecisionService scoreDecisionService) {

        this.booleanDecisionService = booleanDecisionService;
        this.choiceDecisionService = choiceDecisionService;
        this.scoreDecisionService = scoreDecisionService;
    }

    public DecisionResult evaluateBoolean(
            boolean condition, String explanation) {

        return booleanDecisionService.evaluate(condition, explanation);
    }

    public DecisionResult evaluateChoice(
            String selectedOption,
            List<String> availableOptions,
            String explanation) {

        return choiceDecisionService.evaluate(
                selectedOption, availableOptions, explanation);
    }

    public DecisionResult evaluateScore(
            int score, String explanation) {

        return scoreDecisionService.evaluate(score, explanation);
    }
}