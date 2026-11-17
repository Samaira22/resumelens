package com.resumelens.engine;

import com.resumelens.model.JobRequirements;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JobDescriptionParserTest {

    private final JobDescriptionParser parser = new JobDescriptionParser(new SkillExtractor());

    @Test
    void splitsRequiredAndPreferred() {
        JobRequirements r = parser.parse("Required: Java, SQL.\nPreferred: Docker.");
        assertEquals(Set.of("Java", "SQL"), r.required());
        assertEquals(Set.of("Docker"), r.preferred());
    }

    @Test
    void noMarkersMeansEverythingIsRequired() {
        JobRequirements r = parser.parse("We need Java and Docker experience.");
        assertEquals(Set.of("Java", "Docker"), r.required());
        assertEquals(Set.of(), r.preferred());
    }

    @Test
    void requiredWinsWhenSkillIsInBothSections() {
        JobRequirements r = parser.parse("Required: Java.\nNice to have: Java, Docker.");
        assertEquals(Set.of("Java"), r.required());
        assertEquals(Set.of("Docker"), r.preferred());
    }
}
