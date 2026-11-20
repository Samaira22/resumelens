package com.resumelens.engine;

import com.resumelens.model.MatchResult;
import com.resumelens.model.ScoreReport;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

class SuggestionGeneratorTest {

    private final SuggestionGenerator generator = new SuggestionGenerator();

    private ScoreReport report(Set<String> reqMissing, Map<String, String> reqPartial, Set<String> prefMissing) {
        MatchResult required = new MatchResult(Set.of(), reqPartial, reqMissing);
        MatchResult preferred = new MatchResult(Set.of(), Map.of(), prefMissing);
        return new ScoreReport(required, preferred, 0.5);
    }

    @Test
    void missingRequiredSkillComesFirst() {
        List<String> s = generator.generate(
                report(Set.of("Docker"), Map.of(), Set.of("AWS")),
                Set.of(), List.of(), "Built an app");
        assertTrue(s.get(0).contains("Docker"));
    }

    @Test
    void partialMatchSuggestsNamingTheSkill() {
        List<String> s = generator.generate(
                report(Set.of(), Map.of("SQL", "PostgreSQL"), Set.of()),
                Set.of(), List.of(), "Improved speed by 20%");
        assertTrue(s.stream().anyMatch(x -> x.contains("SQL") && x.contains("PostgreSQL")));
    }

    @Test
    void suggestsMeasurableResultsWhenProjectsHaveNoNumbers() {
        List<String> without = generator.generate(
                report(Set.of(), Map.of(), Set.of()), Set.of(), List.of(), "Built a chatbot");
        assertTrue(without.stream().anyMatch(x -> x.contains("measurable")));

        List<String> with = generator.generate(
                report(Set.of(), Map.of(), Set.of()), Set.of(), List.of(), "Improved accuracy by 12%");
        assertTrue(with.stream().noneMatch(x -> x.contains("measurable")));
    }

    @Test
    void missingSectionsGetAdvice() {
        List<String> s = generator.generate(
                report(Set.of(), Map.of(), Set.of()), Set.of(), List.of("EXPERIENCE"), "");
        assertTrue(s.stream().anyMatch(x -> x.contains("Experience section")));
    }

    @Test
    void suggestionsAreCappedAtEight() {
        Set<String> many = new TreeSet<>(Set.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J"));
        List<String> s = generator.generate(
                report(many, Map.of(), Set.of()), Set.of(), List.of(), "");
        assertEquals(8, s.size());
    }
}
