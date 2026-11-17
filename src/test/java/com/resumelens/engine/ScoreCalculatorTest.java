package com.resumelens.engine;

import com.resumelens.model.JobRequirements;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoreCalculatorTest {

    private final ScoreCalculator calculator = new ScoreCalculator(new SkillMatcher());

    @Test
    void allRequiredMatchedAndNoPreferredGivesFullScore() {
        var job = new JobRequirements(Set.of("Java"), Set.of());
        assertEquals(100, calculator.calculate(Set.of("Java"), job).score());
    }

    @Test
    void halfOfRequiredSkillsGivesFifty() {
        var job = new JobRequirements(Set.of("Java", "Docker"), Set.of());
        assertEquals(50, calculator.calculate(Set.of("Java"), job).score());
    }

    @Test
    void requiredCountsMoreThanPreferred() {
        var job = new JobRequirements(Set.of("Java"), Set.of("Docker"));
        // required fully matched (0.75), preferred missing (0)
        assertEquals(75, calculator.calculate(Set.of("Java"), job).score());
    }
}
