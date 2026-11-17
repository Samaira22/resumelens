package com.resumelens.engine;

import com.resumelens.model.MatchResult;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SkillMatcherTest {

    private final SkillMatcher matcher = new SkillMatcher();

    @Test
    void exactMatchIsFullCredit() {
        MatchResult r = matcher.match(Set.of("Java"), Set.of("Java"));
        assertEquals(Set.of("Java"), r.matched());
        assertEquals(1.0, r.coverage());
    }

    @Test
    void relatedSkillGivesPartialCredit() {
        MatchResult r = matcher.match(Set.of("PostgreSQL"), Set.of("SQL"));
        assertEquals("PostgreSQL", r.partial().get("SQL"));
        assertEquals(0.5, r.coverage());
    }

    @Test
    void unrelatedSkillIsMissing() {
        MatchResult r = matcher.match(Set.of("HTML"), Set.of("Java"));
        assertTrue(r.missing().contains("Java"));
        assertEquals(0.0, r.coverage());
    }

    @Test
    void emptyRequirementsGiveZeroCoverage() {
        MatchResult r = matcher.match(Set.of("Java"), Set.of());
        assertEquals(0.0, r.coverage());
    }
}